package org.enoch.snark.gi;

import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;
import org.junit.After;
import org.junit.Before;

public class AbstractSeleniumTest {

    public static final String TEST_UNIVERSUM = "Spica";

    protected Instance instance;
    protected Planet sampleTarget;

    public AbstractSeleniumTest() {
    }

    protected AbstractSeleniumTest(String universeRepository) {
//        this.universeRepository = universeRepository;
//        instance = creatTestInstance();
//
//        sampleTarget = new Planet("[3:104:7]");
    }

    public Instance creatTestInstance() {
//        for(UniverseEntity universeEntity : universeRepository.findAll()) {
//            if(universeEntity.getName().equals(TEST_UNIVERSUM)){
//                return new Instance(universeEntity, false);
//            }
//        }
        return null;
    }

    @Before
    public void setUp() {
//        instance.session.open();
    }

    @After
    public void tearDown() {
        instance.session.close();
    }
}
