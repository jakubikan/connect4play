import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import connectfour.controller.GameController;
import connectfour.controller.IController;
import connectfour.persistence.ISaveGameDAO;
import connectfour.persistence.db4o.SaveGameDb4oDAO;
import play.Application;
import play.GlobalSettings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jakub
 * Date: 11/6/13
 * Time: 11:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class Global extends GlobalSettings {

    private Injector injector;

    @Override
    public void onStart(Application application) {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {


                bind(IController.class).to(GameController.class);
                bind(ISaveGameDAO.class).to(SaveGameDb4oDAO.class);

            }
        });
    }

    @Override
    public <T> T getControllerInstance(Class<T> aClass) throws Exception {
        return injector.getInstance(aClass);
    }
}
