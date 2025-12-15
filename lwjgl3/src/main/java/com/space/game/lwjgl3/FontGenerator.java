package com.space.game.lwjgl3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter;

public class FontGenerator extends ApplicationAdapter {
  @Override
  public void create() {
    System.out.println("Starting Font Generation...");
    try {
      // Generate Space Age (re-generating just in case, or for completeness)
      // generateFont("space-age.ttf", "space-age", 30);
      // generateFont("space-age.ttf", "space-age", 100);
      // generateFont("space-age.ttf", "space-age", 150);

      // Generate Nasalization
      generateFont("nasalization-rg.otf", "nasalization", 30);
      generateFont("nasalization-rg.otf", "nasalization", 100);
      generateFont("nasalization-rg.otf", "nasalization", 150);

      System.out.println("Font Generation Completed Successfully!");
    } catch (Exception e) {
      e.printStackTrace();
    }
    Gdx.app.exit();
  }

  private void generateFont(String fontFileName, String outputName, int size) {
    System.out.println("Generating " + outputName + " size " + size + "...");
    FileHandle fontFile = Gdx.files.internal("fonts/" + fontFileName);
    if (!fontFile.exists()) {
      System.err.println("Font file not found: " + fontFile.path());
      return;
    }

    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
    FreeTypeFontParameter parameter = new FreeTypeFontParameter();
    parameter.size = size;
    parameter.color = Color.WHITE;
    parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS;

    // Use a packer to capture the glyphs
    // increased size for larger fonts to avoid multiple pages if possible, but 1024
    // is usually safe/compatible.
    PixmapPacker packer = new PixmapPacker(2048, 2048, Format.RGBA8888, 2, false);
    parameter.packer = packer;

    BitmapFont font = generator.generateFont(parameter);

    FileHandle fontDir = Gdx.files.local("fonts/");
    String fontOutputName = outputName + "-" + size;

    BitmapFontWriter.setOutputFormat(BitmapFontWriter.OutputFormat.Text);
    String[] pageRefs = BitmapFontWriter.writePixmaps(packer.getPages(), fontDir, fontOutputName);

    // Write the font file
    BitmapFontWriter.writeFont(font.getData(), pageRefs, fontDir.child(fontOutputName + ".fnt"),
        new BitmapFontWriter.FontInfo(), packer.getPageWidth(), packer.getPageHeight());

    packer.dispose();
    // font.dispose();
    generator.dispose();
  }
}
