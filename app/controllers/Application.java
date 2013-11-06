package controllers;

import connectfour.controller.GameController;
import play.mvc.Controller;
import play.mvc.Result;
import connectfour.model.Player;
import views.html.index;


public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Connect 4 Pl(us/ay)"));
    }

}
