package ru.job4j;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.utils.HabrCareerDateTimeParser;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private final Properties cfg = new Properties();

    private static final String LINK = "https://career.habr.com/vacancies/java_developer?page=";

    /**Метод возвращает объект Store
     *с параметрами cfg.
     * @return объект Store
     * @throws SQLException
     */
    public Store store() throws SQLException {
        return new PsqlStore(cfg);
    }

    /**Создает и запускает объект Scheduler.
     * @return
     * @throws SchedulerException
     */
    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    /**Читает файл конфигурации,
     *сохроняет параметры в поле cfg.
     */
    public void cfg() {
        try (InputStream in = Grabber.class.getClassLoader().getResourceAsStream("app.properties")) {
            cfg.load(in);
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }

    /**Принимает объекты Parse, Store, Scheduler.
     *Формирует объект Job c Store, Parse.
     *Формирует расписание с периодом повторения
     *и длительностью работы.
     *Формирует тригер с точкой старта и расписанием.
     * @param parse
     * @param store
     * @param scheduler
     * @throws SchedulerException
     */
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
        /**Принимает контект работы.
         *Передает в {@link HabrCareerParse#list(String)}
         *шаблон ссылки на страницу с вакансиями.
         *В цикле проходит по List и сохраняет
         *объекты Post в БД.
         * @param context
         */
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

    /**Поднимает сервер на порте указанном
     *в файле конфигурации.
     *Подключает сокет к серверу.
     *В цикле построчно передает содержание записей
     *из БД в браузер.
     * @param store
     */
    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(cfg.getProperty("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes(Charset.forName("Windows-1251")));
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**Запускает работу парсера
     *последовательно вызывая методы.
     * @param args
     */
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
        grab.web(store);
    }
}