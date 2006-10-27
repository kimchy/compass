package org.compass.core.test.component.inheritance1;

public class NonFavouriteSonImpl extends SonImpl implements NonFavouriteSon {

    private NonFavouriteSonImpl() {
    }

    public NonFavouriteSonImpl(String name, Father father) {
        super(name, father);
    }

    public String getName() {
        return super.getName();
    }

    public Father getFather() {
        return super.getFather();
    }
}
