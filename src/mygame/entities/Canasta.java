package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import mygame.util.TextureUtils;

public class Canasta extends Node {

    public static final float NATIVE_WIDTH = 32;
    public static final float NATIVE_HEIGHT = 32;
    public static final float WIDTH = NATIVE_WIDTH * TextureUtils.PIXEL_SCALE;
    public static final float HEIGHT = NATIVE_HEIGHT * TextureUtils.PIXEL_SCALE;

    public Canasta(AssetManager assetManager) {
        super("Basket");

        Geometry back = createLayer(assetManager, "Textures/Canasta2.png", 0);
        Geometry front = createLayer(assetManager, "Textures/Canasta1.png", 2);

        attachChild(back);
        attachChild(front);
    }

    private Geometry createLayer(AssetManager assetManager, String texturePath, float z) {
        Texture tex = TextureUtils.loadPixelArtTexture(assetManager, texturePath);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

        Geometry geom = new Geometry("BasketLayer", new Quad(WIDTH, HEIGHT));
        geom.setMaterial(mat);
        geom.setQueueBucket(Bucket.Gui);
        geom.setLocalTranslation(0, 0, z);
        return geom;
    }

    public void setX(float x) {
        setLocalTranslation(x, getLocalTranslation().y, getLocalTranslation().z);
    }

    public float getX() {
        return getLocalTranslation().x;
    }

    public float getY() {
        return getLocalTranslation().y;
    }
}
