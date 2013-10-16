package controllers;

import play.*;
import play.mvc.*;

import views.html.*;
import connectfour.controller.GameController;



public class Application extends Controller {

    public static Result index() {

        GameController g = GameController.getInstance();

        g.newGame();

        return ok(index.render("Your new application is ready."));
    }

}
