package mygame.util;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;

public class TextureUtils {

    public static final float PIXEL_SCALE = 8f;
    
    public static Texture loadPixelArtTexture(AssetManager assetManager, String path) {
        Texture tex = assetManager.loadTexture(path);
        tex.setMagFilter(Texture.MagFilter.Nearest);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        tex.setWrap(Texture.WrapMode.EdgeClamp);
        return tex;
    }
}