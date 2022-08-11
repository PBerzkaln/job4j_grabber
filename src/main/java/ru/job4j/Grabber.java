package ru.job4j;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.utils.HabrCareerDateTimeParser;
import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private final Properties cfg = new Properties();

    private static final String LINK = "https://career.habr.com/vacancies/java_developer?page=";

    public Store store() throws SQLException {
        return new PsqlStore(cfg);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public void cfg() {
        try (InputStream in = Grabber.class.getClassLoader().getResourceAsStream("app.properties")) {
            cfg.load(in);
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            List<Post> vacancies = null;
            try {
                vacancies = parse.list(LINK);
            } catch (IOException e) {
                throw new IllegalArgumentException();
            }
            for (var v : vacancies) {
                store.save(v);
            }
        }
    }

    public static void main(String[] args) {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = null;
        try {
            scheduler = grab.scheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        Store store = null;
        try {
            store = grab.store();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            grab.init(new HabrCareerParse(new HabrCareerDateTimeParser()), store, scheduler);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}