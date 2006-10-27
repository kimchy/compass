package org.compass.core.test.component.inheritance1;

import org.compass.core.Compass;
import org.compass.core.CompassHit;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;

public class Moo {

    public static void main(String[] args) {
        new Moo().moo();
    }

    private void moo() {

        FatherImpl father = new FatherImpl("Sir Ivan");
        FavouriteSonImpl favouriteSon = new FavouriteSonImpl("Ivan Jr", father);
        father.setFavouriteSon(favouriteSon);

        DaughterImpl daughter = new DaughterImpl("Betty Jr", father);
        father.getChildren().add(daughter);


        Compass compass = (Compass) new CompassConfiguration()
                .setSetting(CompassEnvironment.CONNECTION, "target/test-index")
                .addResource("org/compass/core/test/component/inheritance1/Father.cpm.xml")
                .addResource("org/compass/core/test/component/inheritance1/Child.cpm.xml")
                .buildCompass();
        CompassSession session = compass.openSession();
        CompassTransaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.save(father);
            session.save(favouriteSon);
            session.save(daughter);
        } finally {
            if (transaction != null) {
                transaction.commit();
            }
            if (session != null) {
                session.close();
            }
        }

        session = compass.openSession();
        try {
            transaction = session.beginTransaction();
            CompassHits hits = session.find("+alias:father betty");
            for (int i = 0; i < hits.length(); i++) {
                CompassHit compassHit = hits.hit(i);
                Father data = (Father) compassHit.getData();
                System.out.println("data = " + data.getClass());
                System.out.println("data.getChildren().size() = " + data.getChildren().size());
            }
        } finally {
            if (transaction != null) {
                transaction.commit();
            }
            if (session != null) {
                session.close();
            }
        }
        System.out.println("-----------------------------------");
        session = compass.openSession();
        try {
            transaction = session.beginTransaction();
            CompassHits hits = session.find("+childalias:child iva");
            for (int i = 0; i < hits.length(); i++) {
                CompassHit compassHit = hits.hit(i);
                Object data = compassHit.getData();
                System.out.println("data = " + data.getClass());
            }
        } finally {
            if (transaction != null) {
                transaction.commit();
            }
            if (session != null) {
                session.close();
            }
        }
        System.out.println("-----------------------------------");
        session = compass.openSession();
        try {
            transaction = session.beginTransaction();
            String alias1 = "favouriteson";
            String alias2 = "daughter";
            String alias = buildAlias(new String[]{alias1, alias2});
            CompassHits hits = session.find(alias + " jr");
            for (int i = 0; i < hits.length(); i++) {
                CompassHit compassHit = hits.hit(i);
                Child data = (Child) compassHit.getData();
                System.out.println("data = " + data.getClass());
                System.out.println("data.getFather() = " + data.getFather());
            }
        } finally {
            if (transaction != null) {
                transaction.commit();
            }
            if (session != null) {
                session.close();
            }
        }

        System.out.println("-----------------------------------");
        session = compass.openSession();
        try {
            transaction = session.beginTransaction();
            String alias = buildAlias(new String[]{"father"});
            CompassHits hits = session.find(alias + " sir");
            for (int i = 0; i < hits.length(); i++) {
                CompassHit compassHit = hits.hit(i);
                Object data = compassHit.getData();
                System.out.println("data = " + data.getClass());
            }
        } finally {
            if (transaction != null) {
                transaction.commit();
            }
            if (session != null) {
                session.close();
            }
        }

    }

    private String buildAlias(String[] aliasList) {
        if (aliasList.length == 0) {
            return "";
        }
        StringBuffer buf = new StringBuffer("+(");
        for (int i = 0; i < aliasList.length; i++) {
            buf.append("alias:").append(aliasList[i]).append(" ");
        }
        buf.append(")");
        return buf.toString();
    }

}
