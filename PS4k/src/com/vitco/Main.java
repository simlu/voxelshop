package com.vitco;

import com.vitco.util.action.ActionManager;
import com.vitco.util.error.ErrorHandler;
import com.vitco.util.pref.Preferences;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Initially executed class
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // some licences I found online
        //com.jidesoft.utils.Lm.verifyLicense("Gareth Pidgeon", "ZoeOS", "DJoqM6VZ5apzIiGYUqwaFfnAXmREFrm1");
        //com.jidesoft.utils.Lm.verifyLicense("Marios Skounakis", "JOverseer", "L1R4Nx7vEp0nMbsoaHdH7nkRrx5F.dO");
        //com.jidesoft.utils.Lm.verifyLicense("Softham", "White Label Forex Trading System", "B0QACFIA9DMfBogl8KvwJkUd6fDUzaD2");
        //com.jidesoft.utils.Lm.verifyLicense("Marc Fiume", "Savant Genome Browser", "1BimsQGmP.vjmoMbfkPdyh0gs3bl3932");
        //com.jidesoft.utils.Lm.verifyLicense("Bill Snyder", "CashForward", "U4Fnx9Ak6M1DGKsRXc2fNF8nTG0c2aC");

        if ((args.length > 0) && args[0].equals("debug")) {
            ErrorHandler.setDebugMode();
        }

        // build the application
        final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("com/vitco/logic/config.xml");

        // debug
        ((ActionManager) context.getBean("ActionManager")).performValidityCheck();

        // add a hook
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run()
            {
                // make reference so Preferences object doesn't get destroyed
                Preferences pref = ((Preferences) context.getBean("Preferences"));
                // trigger @PreDestroy
                context.close();
                // store the preferences (this needs to be done here, b/c
                // some PreDestroys are used to store preferences!)
                pref.save();
            }
        });

        // for testing
        // throw new Exception("msg");
    }
}
