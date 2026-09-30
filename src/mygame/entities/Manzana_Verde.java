package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import mygame.util.TextureUtils;

public class Manzana_Verde extends Geometry {

    public static final float NATIVE_SIZE = 24;
    public static final float SIZE = NATIVE_SIZE * TextureUtils.PIXEL_SCALE;
    public static final float FALL_SPEED = 500;

    private boolean caught = false;

    public Manzana_Verde(AssetManager assetManager) {
        super("GreenApple", new Quad(SIZE, SIZE));
        
        Texture tex = TextureUtils.loadPixelArtTexture(assetManager, "Textures/Manzana_Verde.png");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        setMaterial(mat);
        setQueueBucket(Bucket.Gui);
    }

    public void update(float tpf) {
        move(0, -FALL_SPEED * tpf, 0);
    }

    public boolean isCaught() { return caught; }
    public void setCaught(boolean caught) { this.caught = caught; }

    public float getX() { return getLocalTranslation().x; }
    public float getY() { return getLocalTranslation().y; }
}
