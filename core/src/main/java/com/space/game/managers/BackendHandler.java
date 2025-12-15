package com.space.game.managers;

public interface BackendHandler {
  interface Callback {
    void onReady();
  }

  void checkBackend(Callback callback);
}
