/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.argonavis.eipcourse.channel.p2p.camel;

import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 * @author helderdarocha
 */
public class CamelChannels {
	
	public static String ACTIVEMQ_URL = "tcp://localhost:61616";

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        ConnectionFactory cf = new ActiveMQConnectionFactory(ACTIVEMQ_URL);
        context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(cf));

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jms:queue:test-queue") 
                .to("direct:a");
                
                from("jms:queue:test-queue") 
                .to("seda:b");
                
                from("jms:queue:test-queue") 
                .to("vm:c");

                from("vm:c")
                .process(new Processor() {
                    @Override
                    public void process(Exchange ex) {
                        System.out.println("Camel vm:c received: " + ex.getIn().getBody(String.class));
                    }
                });
                from("seda:b")
                .process(new Processor() {
                    @Override
                    public void process(Exchange ex) {
                        System.out.println("Camel seda:b received: " + ex.getIn().getBody(String.class));
                    }
                });
                from("direct:a")
                .process(new Processor() {
                    @Override
                    public void process(Exchange ex) {
                        System.out.println("Camel direct:a received: " + ex.getIn().getBody(String.class));
                    }
                });
            }
        });
        context.start();

        System.out.println("O servidor está no ar por 60 segundos.");
        Thread.sleep(60000);
        context.stop();
    }
}
