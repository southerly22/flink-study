import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;

/**
 * @author lzx
 * @date 2023/3/13 11:47
 * @description: TODO 根据表生成 Insert SQL语句
 */
public class Insert {
    public static void main(String[] args) throws IOException {
        String sampleInfoSql =
                "INSERT INTO DIM_SAMPLE_INFO_MAPPING\n" +
                        "SELECT\n" +
                        "  sha1,\n" +
                        "  addtime,\n" +
                        "  id\n" +
                        "FROM DIM_SAMPLE_INFO\n" +
                        "WHERE id IS NOT NULL";

        LocalDate current = LocalDate.now();
        LocalDate first = LocalDate.parse("2014-08-11");


        generateSql(current, first, sampleInfoSql, "sampleInfoMapping");
    }

    public static void generateSql(LocalDate current, LocalDate first, String sql, String fileName) throws IOException {
        String s1 = "";
        String s2 = "";
        StringBuilder finalSql = new StringBuilder();
        while (!current.isBefore(first)) {
            StringBuilder stringBuilder = new StringBuilder(sql);
            LocalDate nextFirst = first.with(TemporalAdjusters.firstDayOfNextMonth());
            s1 = " and addtime >= '" + first + "'";
            s2 = " and addtime < '" + nextFirst + "';";
            stringBuilder.append(s1);
            stringBuilder.append(s2);
            finalSql.append(stringBuilder);
            finalSql.append("\n");
            first = nextFirst;
        }
        System.out.println(finalSql);
        Files.write(Paths.get("C:\\Users\\HR\\Desktop\\" + fileName + ".sql"), finalSql.toString().getBytes());
    }
}
