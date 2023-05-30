package tools;

import java.util.Optional;

/**
 * @author lzx
 * @date 2023/05/28 20:00
 **/
public class OptionalHander {


    public String getCountry(User user){
        return user.getCompany().getAddress().getCountry();
    }

    public String getCountryOptional(User user){
        Optional<User> optionalUser = Optional.ofNullable(user);
        return optionalUser.map(User::getCompany)
                .map(Company::getAddress)
                .map(Address::getCountry)
                .orElseGet(this::getCountryName); // orElseGet:先执行链式调用，为空的话在执行该方法
                //.orElse(getCountryName()); // 先 orElse里的方法，无论链式调用的值是否为空，该方法一定会执行
    }

    public String getCountryName(){
        //System.out.println("开始 执行 getCountryName 方法");
        return "China";
    }

    public static void main(String[] args) {
        Address address = new Address("USA");
        //Address address = null;
        Company company = new Company("apple",address);
        User user = new User("Tim",20,company);

        OptionalHander hander = new OptionalHander();
        //String country = hander.getCountry(user);
        String country = hander.getCountryOptional(user);
        System.out.println("country = " + country);

    }
}
