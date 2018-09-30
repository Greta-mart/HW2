package ua.skillsup.practice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.*;

public class Service implements ExampleService {

    private ExampleDao exampleDao;
    private final BigDecimal limit =  new BigDecimal(15.00);

    public Service(ExampleDao exampleDao) {
        this.exampleDao = exampleDao;
    }

    @Override
    public void addNewItem(String title, BigDecimal price) {
        if (title == null || title.isEmpty())
            return;
        for (ExampleEntity list: exampleDao.findAll()) {
            if(list.getTitle().equals(title))
                return;
        }
        if (title.length() < 3 || title.length() > 20)
            return;


        if (price == null)
            return;
        if (price.compareTo(limit) == -1)
            return;
        ExampleEntity exampleEntity = new ExampleEntity();
        exampleEntity.setTitle(title);
        exampleEntity.setPrice(price.setScale(2, RoundingMode.HALF_UP));
        exampleDao.store(exampleEntity);
    }

    /**
     * Prepare storage statistic of items average prices per day they were added.
     * In case no items were added in concrete day - the day shouldn't be present in the final result
     * @return {@link Map} of statistic results, where key is the day when items were stored, and
     *      the value is actual average cost of all items stored during that day
     */
    @Override
    public Map<LocalDate, BigDecimal> getStatistic() {
        Map<LocalDate, ArrayList<ExampleEntity>> map = new HashMap<>();
        for (ExampleEntity entity:exampleDao.findAll()) {
            Instant instant = entity.getDateIn();
            LocalDate date = LocalDate.of(instant.get(ChronoField.YEAR), instant.get(ChronoField.MONTH_OF_YEAR), instant.get(ChronoField.DAY_OF_MONTH));
            if (map.containsKey(date))
                map.get(date).add(entity);
            else {
                map.put(date, new ArrayList<>());
                map.get(date).add(entity);
            }
        }
        Map<LocalDate, BigDecimal> result = new HashMap<>();
        for (LocalDate date:map.keySet()) {
            BigDecimal sum = new BigDecimal(0.00);
            ArrayList<ExampleEntity> temp = map.get(date);
            for (ExampleEntity entity:temp) {
                sum = entity.getPrice().add(sum);
            }
            result.put(date, sum.divide(new BigDecimal(temp.size())));
        }
        return result;
    }
}
