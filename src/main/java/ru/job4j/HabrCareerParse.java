package ru.job4j;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.quartz.AlertRabbit;
import ru.job4j.utils.DateTimeParser;
import ru.job4j.utils.HabrCareerDateTimeParser;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HabrCareerParse implements Parse {

    private final DateTimeParser dateTimeParser;

    public static final int PAGE_COUNT = 5;

    private static final String SOURCE_LINK = "https://career.habr.com";

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws SQLException {
        HabrCareerParse habr = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> vacancies = habr.list("https://career.habr.com/vacancies/java_developer?page=");
        System.out.println(vacancies.size());
        System.out.println(vacancies.get(0));
        Properties properties = new Properties();
        try (InputStream fis = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PsqlStore psql = new PsqlStore(properties);
        psql.save(vacancies.get(0));
        System.out.println(psql.getAll());
    }

    private String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        Document document = null;
        try {
            document = connection.get();
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
        Element descElement = document.select(".style-ugc").first();
        return descElement.text();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> savedPages = new ArrayList<>();
        for (int i = 1; i <= PAGE_COUNT; i++) {
            String pages = String.format("%s%d", link, i);
            try {
                Connection connection = Jsoup.connect(pages);
                Document document = null;
                document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                for (Element row : rows) {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    Element dateElement = row.select(".vacancy-card__date").first();
                    Element date = dateElement.child(0);
                    LocalDateTime vacancyDate = dateTimeParser.parse(date.attr("datetime"));
                    String link1 = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                    String desc = retrieveDescription(link1);
                    savedPages.add(new Post(vacancyName, link1, desc, vacancyDate));
                }
            } catch (IOException e) {
                throw new IllegalArgumentException();
            }
        }
        return savedPages;
    }
}