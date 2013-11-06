window.Connectfour = Ember.Application.create( {
  LOG_TRANSITIONS: true
});

Connectfour.Router.reopen {
  rootURL: '/api/'
}



Connectfour.Store = DS.Store.extend {
  revision: 12
}



DS.RESTAdapter.reopen({
  namespace: 'api'
});

Connectfour.Router.map  ->
  @resource "games", ->
    @route "new", {path: "new/:name"}
    @route "play", {path: "/:name"}
    @route "dropcoin", {path: ":name/dropCoin/:column"}
    @route "save", {path: "save/:name"}
    @route "load", {path: "load/:name"}

DS.RESTAdapter.registerTransform 'raw', {
  deserialize: (serialized) ->
    return serialized;

  serialize: (deserialized) ->
    return deserialized;
}

Connectfour.Game = DS.Model.extend {
  name: DS.attr "string"
  gameField: DS.attr "raw"
}

Connectfour.Games = DS.Model.extend {
  games: DS.hasMany "Connectfour.Game"
}

Connectfour.GamesRoute = Ember.Route.extend {
  model: (params) ->
}



Connectfour.GamesNewRoute = Ember.Route.extend {
  model: (params)->
    store = @get "store"
    newGameObj = {
      name: params.name
      gameField: []
    }
    newGame = Connectfour.Game.createRecord (newGameObj)
    newGame.save();
    return newGame;

  setupController: (controller, model) ->
    controller.set 'model', model

  afterModel: (params, a, b, c)->
    store = @get "store"
    game = store.find "game", a.params.name
    @transitionTo "games.play", game

}

Connectfour.GamesLoadRoute = Ember.Route.extend {
  model: (params) ->
}


Connectfour.GamesSaveRoute = Ember.Route.extend {
  model: (params) ->
}

Connectfour.GamesPlayRoute = Ember.Route.extend {
  model: (params) ->
    store = @get "store"
    return store.find "game", params.name
  setupController: (controller, model) ->
    controller.set('model', model);
  actions: {
    dropCoin: (column, view) ->
      $.getJSON "/api/games/#{@currentModel.id}/dropcoin/#{column}"
      view.propertyDidChange()
      view.rerender()
  }

}

















