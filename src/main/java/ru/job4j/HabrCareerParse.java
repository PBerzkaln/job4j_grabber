package ru.job4j;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.utils.DateTimeParser;
import ru.job4j.utils.HabrCareerDateTimeParser;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String PAGE_LINK = "/vacancies/java_developer";

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
       HabrCareerParse habr = new HabrCareerParse(new HabrCareerDateTimeParser());
       List<Post> vacancies = habr.list("https://career.habr.com");
        System.out.println(vacancies.size());
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element descElement = document.select(".style-ugc").first();
        return descElement.text();
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> savedPages = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String pages = String.format("%s%s?page=%d", link, PAGE_LINK, i);
            Connection connection = Jsoup.connect(pages);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            for (Element row : rows) {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                Element dateElement = row.select(".vacancy-card__date").first();
                Element date = dateElement.child(0);
                LocalDateTime vacancyDate = dateTimeParser.parse(date.attr("datetime"));
                String link1 = String.format("%s%s", link, linkElement.attr("href"));
                String desc = retrieveDescription(link1);
                savedPages.add(new Post(vacancyName, link1, desc, vacancyDate));
            }
        }
        return savedPages;
    }
}