package controllers;

import com.google.inject.Inject;
import connectfour.persistence.ISaveGameDAO;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;


public class Application extends Controller {

    @Inject
    ISaveGameDAO s;

    public Result index() {
        return ok(index.render("Connect 4 Pl(us/ay)"));
    }

}
