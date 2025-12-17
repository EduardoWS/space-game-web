package com.space.game.managers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.LinkedList;
import java.util.Iterator;

public class FeedbackQueue {

  private static class Message {
    String text;
    Color color;
    float life;
    float maxLife;

    public Message(String text, Color color, float life) {
      this.text = text;
      this.color = new Color(color); // Copy color
      this.life = life;
      this.maxLife = life;
    }
  }

  private LinkedList<Message> messages;
  private final float MESSAGE_LIFE = 2.0f;
  private final float MAX_MESSAGES = 5;

  public FeedbackQueue() {
    this.messages = new LinkedList<>();
  }

  public void addMessage(String text, Color color) {
    messages.addFirst(new Message(text, color, MESSAGE_LIFE));
    if (messages.size() > MAX_MESSAGES) {
      messages.removeLast();
    }
  }

  public void update(float dt) {
    Iterator<Message> it = messages.iterator();
    while (it.hasNext()) {
      Message msg = it.next();
      msg.life -= dt;
      if (msg.life <= 0) {
        it.remove();
      }
    }
  }

  public void clear() {
    messages.clear();
  }

  /**
   * Renders the queue.
   * 
   * @param batch   SpriteBatch
   * @param font    BitmapFont to use
   * @param startX  X position
   * @param startY  Y position
   * @param upwards If true, stack upwards (for bottom HUD). If false, stack
   *                downwards (for top HUD).
   */
  public void render(SpriteBatch batch, BitmapFont font, float startX, float startY, boolean upwards) {
    float currentY = startY;
    float spacing = font.getLineHeight() * 1.2f;

    for (Message msg : messages) {
      float alpha = msg.life / msg.maxLife;
      // Fade out faster at end
      if (alpha > 1f)
        alpha = 1f;

      Color c = msg.color;
      font.setColor(c.r, c.g, c.b, alpha);

      font.draw(batch, msg.text, startX, currentY);

      if (upwards) {
        currentY += spacing;
      } else {
        currentY -= spacing;
      }
    }
    // Restore color
    font.setColor(Color.WHITE);
  }
}
