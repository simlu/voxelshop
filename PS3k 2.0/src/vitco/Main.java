package vitco;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: VM Win 7
 * Date: 7/14/12
 * Time: 10:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    public static void main(String[] args) {
        BeanFactory beanfactory = (BeanFactory) new ClassPathXmlApplicationContext("spring/config.xml");
    }
}
