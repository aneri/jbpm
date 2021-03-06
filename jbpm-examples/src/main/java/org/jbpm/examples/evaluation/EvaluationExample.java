package org.jbpm.examples.evaluation;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.workitem.wsht.HornetQHTWorkItemHandler;

public class EvaluationExample {

    public static final void main(String[] args) {
        try {
            // load up the knowledge base
            KnowledgeBase kbase = readKnowledgeBase();
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
            final KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newThreadedFileLogger(ksession, "test", 1000);
            final HornetQHTWorkItemHandler hornetQHTWorkItemHandler = new HornetQHTWorkItemHandler(ksession);
            Runtime.getRuntime().addShutdownHook(new Thread() {

                public void run() {
                    if (logger != null) {
                        try {
                            hornetQHTWorkItemHandler.dispose();
                        } catch (Exception ex) {
                            Logger.getLogger(EvaluationExample.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        logger.close();
                        System.exit(0);
                    }
                }
            });

            ksession.getWorkItemManager().registerWorkItemHandler("Human Task", hornetQHTWorkItemHandler);
            // start a new process instance
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("employee", "krisv");
            params.put("reason", "Yearly performance evaluation");
            ksession.startProcess("com.sample.evaluation", params);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static KnowledgeBase readKnowledgeBase() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("evaluation/Evaluation.bpmn"), ResourceType.BPMN2);
        return kbuilder.newKnowledgeBase();
    }
}
