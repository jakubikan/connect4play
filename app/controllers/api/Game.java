package controllers.api;

import com.fasterxml.jackson.databind.JsonNode;


import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Injector;
import connectfour.controller.GameController;
import connectfour.controller.IController;
import connectfour.model.Computer;
import connectfour.model.Human;
import connectfour.model.Player;
import connectfour.model.SaveGame;
import connectfour.persistence.ISaveGameDAO;
import connectfour.util.observer.IObserverWithArguments;
import models.GameModel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static play.data.Form.form;


/**
 * Created with IntelliJ IDEA.
 * User: jakub
 * Date: 10/21/13
 * Time: 11:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class Game extends Controller {

    @Inject
    private Injector injector;
    @Inject
    private ISaveGameDAO saveGameDAO;

    @Inject
    private Map<String, GameModel> gamesMap = new ConcurrentHashMap<>();

    @BodyParser.Of(BodyParser.Json.class)
    public Result newGameWithoutName() {
        DynamicForm requestData = form().bindFromRequest();
        Result result;
        if (requestData.field("game") != null) {

            Form.Field f = requestData.field("game");
            String playerVsPlayer =  f.sub("isPlayerVsPlayer").value();
            if (StringUtils.contains(playerVsPlayer, "true"))
                result = newGameWithName(f.sub("id").value(), true);
            else
                result = newGameWithName(f.sub("id").value(), false);

        } else {
            result = newGameWithName(UUID.randomUUID().toString(), false);

        }
        return result;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result newGameWithName(String gameName, boolean isPlayerVsPlayer) {
        IController controller = null;
        try {
            controller = GameController.class.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println(controller);
        GameModel gameModel;
        if (!isPlayerVsPlayer) {
            gameModel = new GameModel(controller);
        } else {
            Player player = new Human("You");
            gameModel = new GameModel(controller, player);
        }
        Result result = newGame(gameName, gameModel);

        return result;

    }



    @BodyParser.Of(BodyParser.Json.class)
    public Result newGame(String gameName, GameModel gameModel) {
        GameModel  model = gameModel;
        if (gamesMap.containsKey(gameName)) {
            model = gamesMap.get(gameName);
        }
        boolean hasStartedGame = model.startGame();
        ObjectNode node = Json.newObject();
        gamesMap.put(gameName, model);

        ObjectNode a = node.putObject("game");
        a.put("id", gameName);
        a.put("isPlayerVsPlayer", model.isPlayerVsPlayer());
        a.put("isWaitingForOpponent", model.isWaitingForOpponent());
        a.put("gameStarted", hasStartedGame);
        a.putArray("game_field");

        session("player", Integer.toString(model.getPlayer().hashCode()));
        return ok(node);
    }


    public Result joinGame(String gameName) {
        ObjectNode node = Json.newObject();
        if (gamesMap.containsKey(gameName)) {
            GameModel model = gamesMap.get(gameName);
            model.joinGame(new Human("opponent"));
            if (model.startGame()){
                node.put("gameStarted", true);

            }
            session("player", Integer.toString(model.getOpponent().hashCode()));

        }
        return ok(node);
    }

    public Result dropCoin(String gameName, int column) {
        ObjectNode node = Json.newObject();
        String player =  session("player");
        if (gamesMap.containsKey(gameName)) {
            GameModel model = gamesMap.get(gameName);
            if (model.isStarted()) {
                Player p = model.getPlayerOnTurn();
                Player pp = model.getPlayer();
                Player op = model.getOpponent();
                String hashcodePP = Integer.toString(pp.hashCode());
                String hashcodeOP = Integer.toString(op.hashCode());
                String hashcode = Integer.toString(p.hashCode());
                if (player.equals(hashcode) || !model.isPlayerVsPlayer()) {
                    boolean success = model.getGameController().dropCoinWithSuccessFeedback(column);
                    if (success)
                        node.put("dropped", true);
                    else
                        node.put("dropped", false);
                }
            }
        } else {
            node.put("dropped", false);
        }


        return ok(node);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getGameField(String gameName) {
        if (gamesMap.containsKey(gameName)) {
            GameModel model = gamesMap.get(gameName);
            ObjectNode node = Json.newObject();
            node.put("game", gameFieldToJsonNode(gameName, model));
            return ok(node);

        }
        return badRequest();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getGameFields() {
        ObjectNode node = Json.newObject();
        ArrayNode a = node.putArray("games");
        for (Map.Entry<String, GameModel> s : gamesMap.entrySet()) {
            a.add(gameFieldToJsonNode(s.getKey(), s.getValue()));
        }
        return ok(node);

    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getSavedGames() {
        Collection<GameModel> gc = gamesMap.values();
        IController controller = gc.iterator().next().getGameController();
        List<String> saveGames = controller.getAllSaveGameNames();
        JsonNode node = Json.toJson(saveGames);
        return ok(node);
    }

    public Result loadGame(String gameName, String loadGameName) {
        if (gamesMap.containsKey(gameName)) {
            GameModel model = gamesMap.get(gameName);
            SaveGame sg = saveGameDAO.loadSaveGame(loadGameName);
            model.getGameController().setGameField(sg.getGameField());
            ObjectNode node = Json.newObject();
            node.put("loaded", true);
            return ok(node);
        }
        return badRequest();
    }

    public Result saveGame(String gameName, String saveGameName) throws CloneNotSupportedException {
        System.out.println(gameName);
        System.out.println(saveGameName);
        if (gamesMap.containsKey(gameName)) {
            GameModel model = gamesMap.get(gameName);
            IController controller = model.getGameController();
            SaveGame sg = new SaveGame(saveGameName, controller.getGameField().clone(), controller.getPlayer(), controller.getOpponend());
            saveGameDAO.saveGame(sg);
            ObjectNode node = Json.newObject();
            node.put("saved", true);
            return ok(node);
        } else {
            return badRequest();
        }
    }

    public Result undo(String gameName){
        if (gamesMap.containsKey(gameName)) {
            GameModel model = gamesMap.get(gameName);
            IController controller = model.getGameController();
            controller.undoStep();
            ObjectNode node = Json.newObject();
            node.put("undone", true);
            return ok(node);
        }
        return badRequest();

    }
    public Result redo(String gameName){
        if (gamesMap.containsKey(gameName)) {
            GameModel model = gamesMap.get(gameName);
            IController controller = model.getGameController();
            controller.redoStep();
            ObjectNode node = Json.newObject();
            node.put("undone", true);
            return ok(node);
        }
        return badRequest();

    }


    private ObjectNode gameFieldToJsonNode(String gameName, GameModel gameModel) {
        try {
            IController c = gameModel.getGameController();
            Player[][] gameField = c.getGameField().getCopyOfGamefield();
            ArrayUtils.reverse(gameField);
            ObjectNode node = Json.newObject();
            node.put("id", gameName);
            node.put("isPlayerVsPlayer", gameModel.isPlayerVsPlayer());
            node.put("isWaitingForOpponent", gameModel.isWaitingForOpponent());
            node.put("gameStarted", gameModel.isStarted());
            ArrayNode gameArrayNode = node.putArray("game_field");
            for (Player[] rows : gameField) {
                ArrayNode rowsNode = gameArrayNode.addArray();
                for (Player p : rows) {
                    if (p != null) {
                        if (p == c.getOpponend())
                            rowsNode.add("opponent");
                        else
                            rowsNode.add("you");
                    } else {
                        rowsNode.addNull();
                    }
                }
            }
            return node;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }


}
