import com.google.inject.*;
import com.google.inject.name.Names;
import connectfour.controller.GameController;
import connectfour.controller.IController;
import connectfour.persistence.ISaveGameDAO;
import connectfour.persistence.couchdb.SaveGameCouchDbDAO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import connectfour.persistence.db4o.SaveGameDb4oDAO;
import connectfour.ui.gui.swing.SwingGUI;
import connectfour.ui.tui.TUI;
import connectfour.util.observer.IObserver;
import models.GameModel;
import play.GlobalSettings;

/**
 * Created with IntelliJ IDEA.
 * User: jakub
 * Date: 11/6/13
 * Time: 11:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class Global  extends GlobalSettings {

    private static Injector injector = createInjector();



    @Override
    public <A> A getControllerInstance(Class<A> aClass) {
        return injector.getInstance(aClass);
    }


    private static Injector createInjector() {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ISaveGameDAO.class).to(SaveGameDb4oDAO.class).in(Scopes.SINGLETON);
                bind(IController.class).to(GameController.class).in(Scopes.NO_SCOPE);
                bind(new TypeLiteral<Map<String, GameModel>>(){})
                        .to(new TypeLiteral<ConcurrentHashMap<String, GameModel>>(){})
                        .in(Scopes.SINGLETON);

            }
        });
    }
}
