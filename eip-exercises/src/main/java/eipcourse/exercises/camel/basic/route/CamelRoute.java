package eipcourse.exercises.camel.basic.route;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

public class CamelRoute {

	public static String ACTIVEMQ_URL = "tcp://localhost:61616";

	public static void main(String[] args) throws Exception {
		CamelContext context = new DefaultCamelContext();
		
	    ConnectionFactory cf = new ActiveMQConnectionFactory(ACTIVEMQ_URL);
	    context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(cf));
		
		context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
            	    // Router
            		from("jms:queue:entrada")
            		.choice()
            		   .when(header("content-type").isEqualTo("text/plain"))
            		      .process((e)->System.out.println("Enviada para saida."))
            		      .to("jms:queue:saida")
            		   .when(header("content-type").isEqualTo("text/xml"))
            		      .process((e)->System.out.println("Enviada para processamento."))
            		      .to("jms:queue:msgxml")
            		   .otherwise()
            		      .process((e)->System.out.println("Enviada para fila de mensages inválidas."))
            		      .to("jms:queue:invalida")
            		      .stop()
            		.end();
            		
            		// Transformer
            		from("jms:queue:msgxml")
            		   .process(new CamelTransformer())
            		      .to("jms:queue:saida");
            		
            		// Receiver
            		from("jms:queue:saida")
            		   .process((exchange) -> {
            			   System.out.println("Type: " + exchange.getIn().getHeader("content-type"));
            			   System.out.println("Contents: " +exchange.getIn().getBody());
            		   });
            }
		});
		
		context.start();
	}

}
