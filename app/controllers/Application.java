package controllers;

import connectfour.controller.GameController;
import play.mvc.Controller;
import play.mvc.Result;
import connectfour.model.Player;



public class Application extends Controller {

    public static Result index() {

        GameController gc = GameController.getInstance();
        gc.newGame();
        return ok(views.html.gamefield.render(gc.getPlayer(),gc.getOpponend(), gc.getGameField()));
    }

}
