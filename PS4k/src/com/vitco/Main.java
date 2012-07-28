package com.vitco;

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
        // some licences I found online
        //com.jidesoft.utils.Lm.verifyLicense("Gareth Pidgeon", "ZoeOS", "DJoqM6VZ5apzIiGYUqwaFfnAXmREFrm1");
        //com.jidesoft.utils.Lm.verifyLicense("Marios Skounakis", "JOverseer", "L1R4Nx7vEp0nMbsoaHdH7nkRrx5F.dO");
        //com.jidesoft.utils.Lm.verifyLicense("Softham", "White Label Forex Trading System", "B0QACFIA9DMfBogl8KvwJkUd6fDUzaD2");
        //com.jidesoft.utils.Lm.verifyLicense("Marc Fiume", "Savant Genome Browser", "1BimsQGmP.vjmoMbfkPdyh0gs3bl3932");
        //com.jidesoft.utils.Lm.verifyLicense("Bill Snyder", "CashForward", "U4Fnx9Ak6M1DGKsRXc2fNF8nTG0c2aC");

        BeanFactory beanfactory = (BeanFactory) new ClassPathXmlApplicationContext("com/vitco/core/config.xml");
    }
}
