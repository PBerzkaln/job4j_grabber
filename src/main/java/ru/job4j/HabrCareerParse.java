package ru.job4j;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    /**В цикле проходим по первым 5 страницам.
     *Сначала мы получаем страницу (Document).
     *Получаем все вакансии страницы (Elements).
     *Получаем нужные элементы (title, link, date).
     *Сохраняем данные в виде строки в list,
     *затем в for-each выводим их на консоль.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        List<String> savedPages = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String pages = String.format("%s?page=%d", PAGE_LINK, i);
            Connection connection = Jsoup.connect(pages);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                Element dateElement = row.select(".vacancy-card__date").first();
                Element date = dateElement.child(0);
                String[] vacancyDate = String.format("%s", date.attr("datetime")).split("T");
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                savedPages.add(String.format("%s %s %s", vacancyDate[0], vacancyName, link));
            });
        }
        for (String s : savedPages) {
            System.out.println(s);
        }
    }
}