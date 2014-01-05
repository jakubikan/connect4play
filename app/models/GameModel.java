package models;

import connectfour.controller.IController;
import connectfour.model.Computer;
import connectfour.model.Human;
import connectfour.model.Player;
import connectfour.util.observer.IObserverWithArguments;
import play.mvc.Http;

import static play.mvc.Http.Context.Implicit.session;

/**
 * Created by jakub on 1/4/14.
 */
public class GameModel {
    private IController gameController;
    private Player player;
    private Player opponent;
    private boolean waitingForOpponent;
    private boolean playerVsPlayer;
    private boolean started;


    /**
     * Standard  GameModel for playing against Computer
     *
     * @param gameController
     */
    public GameModel(IController gameController) {
        this.gameController = gameController;
        player = new Human("you");
        opponent = new Computer((IObserverWithArguments) gameController,"computer");
        waitingForOpponent = false;
        playerVsPlayer = false;
    }

    /**
     * Standard  GameModel for playing against other player
     *
     * @param gameController
     */
    public GameModel(IController gameController, Player player) {
        this.gameController = gameController;
        this.player = player;
        opponent = null;
        waitingForOpponent = true;
        playerVsPlayer = true;
    }

    public boolean startGame() {
        if (!waitingForOpponent && opponent != null) {
            if (opponent instanceof Computer)
                gameController.newGame();
            else if (opponent instanceof Human){
                gameController.newGame(player, opponent);
            }
            started = true;
            return  true;
        }
        return false;
    }

    public boolean joinGame(Player opponent) {
        if (waitingForOpponent) {
            this.opponent = opponent;
            waitingForOpponent = false;
            return  true;
        }
        return false;

    }

    public Player getPlayerOnTurn() {
        return gameController.getPlayerOnTurn();
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public IController getGameController() {
        return gameController;
    }

    public void setGameController(IController gameController) {
        this.gameController = gameController;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public boolean isWaitingForOpponent() {
        return waitingForOpponent;
    }

    public void setWaitingForOpponent(boolean waitingForOpponent) {
        this.waitingForOpponent = waitingForOpponent;
    }

    public boolean isPlayerVsPlayer() {
        return playerVsPlayer;
    }

    public void setPlayerVsPlayer(boolean playerVsPlayer) {
        this.playerVsPlayer = playerVsPlayer;
    }
}
