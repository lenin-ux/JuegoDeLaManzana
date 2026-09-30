package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import mygame.util.TextureUtils;

public class Manzana extends Geometry {

    public enum Tipo { ROJA, VERDE }

    public static final float NATIVE_SIZE = 24;
    public static final float SIZE = NATIVE_SIZE * TextureUtils.PIXEL_SCALE;

    private static final float MIN_SPEED = 250;
    private static final float MAX_SPEED = 350;

    private final Tipo tipo;
    private final float fallSpeed;
    private float multiplicadorVelocidad = 1f;
    private boolean caught = false;

    public Manzana(AssetManager assetManager, Tipo tipo) {
        super("Manzana_" + tipo);
        this.tipo = tipo;
        this.fallSpeed = MIN_SPEED + (float) Math.random() * (MAX_SPEED - MIN_SPEED);

        String texturePath = (tipo == Tipo.ROJA)
                ? "Textures/Manzana_Roja.png"
                : "Textures/Manzana_Verde.png";

        setMesh(new Quad(SIZE, SIZE));

        Texture tex = TextureUtils.loadPixelArtTexture(assetManager, texturePath);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        setMaterial(mat);
        setQueueBucket(Bucket.Gui);
    }

    public void update(float tpf) {
        move(0, -fallSpeed * multiplicadorVelocidad * tpf, 0);
    }

    public void setMultiplicadorVelocidad(float multiplicador) {
        this.multiplicadorVelocidad = multiplicador;
    }

    public Tipo getTipo() { return tipo; }
    public boolean isCaught() { return caught; }
    public void setCaught(boolean caught) { this.caught = caught; }
    public float getX() { return getLocalTranslation().x; }
    public float getY() { return getLocalTranslation().y; }
}