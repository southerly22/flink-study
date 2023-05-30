package tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author lzx
 * @date 2023/05/28 19:54
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true) //开启链式调用
public class User {

    private String name;
    private int age;
    private Company company;
}
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true) //开启链式调用
class Company{
    private String companyName;
    private Address address;
}
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true) //开启链式调用
class Address{
    private String country;
}
