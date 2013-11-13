package controllers.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import connectfour.controller.GameController;
import connectfour.controller.GameField;
import connectfour.controller.IController;
import connectfour.model.Computer;
import connectfour.model.Human;
import connectfour.model.Player;
import connectfour.persistence.ISaveGameDAO;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashMap;
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
    private static ISaveGameDAO saveGameDAO;

    private static Map<String, GameController> gamesMap = new ConcurrentHashMap<>();


    @BodyParser.Of(BodyParser.Json.class)
    public static Result newGameWithoutName() {
        DynamicForm requestData = form().bindFromRequest();
        Result result;
        if (requestData.field("game") != null) {

            Form.Field f =requestData.field("game");
            result = newGameWithName(f.sub("id").value());

        } else {
            result = newGameWithName(UUID.randomUUID().toString());

        }
        return result;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result newGameWithName(String gameName) {
        GameController controller = new GameController();
        Player player = new Human();
        player.setName("You");
        Player computer = new Computer(controller);
        player.setName("Computer");
        Result result = newGame(gameName, player, computer, controller);

        return result;

    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result newGame(String gameName, Player player, Player opponent, GameController c) {
        c.setPlayer(player);
        c.setOpponend(opponent);
        c.newGame();
        GameController controller = c;
        if (gamesMap.containsKey(gameName)) {
            controller = gamesMap.get(gameName);
        }
        ObjectNode node = Json.newObject();
        gamesMap.put(gameName, controller);
        ObjectNode a = node.putObject("game");
        a.put("id", gameName);
        a.putArray("game_field");
        return ok(node);
    }

    public static Result dropCoin(String gameName, int column) {
        ObjectNode node = Json.newObject();
        if (gamesMap.containsKey(gameName) ) {
            IController controller =  gamesMap.get(gameName);
            boolean success = controller.dropCoinWithSuccessFeedback(column);
            if (success)
                node.put("dropped", true);
            else
                node.put("dropped", false);
        } else {
            node.put("dropped", false);
        }


        return ok(node);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getGameField(String gameName) {
        if (gamesMap.containsKey(gameName)) {
            IController controller = gamesMap.get(gameName);
            ObjectNode node = Json.newObject();
            node.put("game", gameFieldToJsonNode(gameName, controller));
            return ok(node);

        }
        return badRequest();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getGameFields() {
        ObjectNode node = Json.newObject();
        ArrayNode a = node.putArray("games");
        for (Map.Entry<String, GameController> s : gamesMap.entrySet()) {
            a.add(gameFieldToJsonNode(s.getKey(),s.getValue()));
        }
       return ok(node);

    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getSavedGames() {
        List<String> saveGames = saveGameDAO.getAllSaveGames();
        JsonNode node = Json.toJson(saveGames);
        return ok(node);
    }

    public static Result loadGame(String gameName, String loadGameName) {
        IController controller = gamesMap.get(gameName);
        controller.loadSaveGame(gameName);
        ObjectNode node = Json.newObject();
        node.put("loaded", true);
        return ok(node);
    }

    public static Result saveGame(String gameName, String saveGameName) {
        IController controller = gamesMap.get(gameName);
        controller.saveGame(gameName);
        ObjectNode node = Json.newObject();
        node.put("saved", true);
        return ok(node);
    }

    private static ObjectNode gameFieldToJsonNode(String gameName, IController c) {
        try {
            Player[][] gameField = c.getGameField().getCopyOfGamefield();
            ObjectNode node = Json.newObject();
            node.put("id", gameName);
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