/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mygame.util;

/**
 *
 * @author lenin
 */

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import mygame.util.TextureUtils;

public class TextoFlotanteEfectos extends Geometry{
    private float lifeTime = 0f;
    private final float maxLifeTime = 0.6f; // Duración total del destello en segundos
    private final float floatSpeed = 80f;    // Velocidad a la que flota hacia arriba
    private final Material mat;

    public TextoFlotanteEfectos(AssetManager assetManager, String texturePath, float x, float y) {
        super("FloatingEffect", new Quad(40 * TextureUtils.PIXEL_SCALE, 40 * TextureUtils.PIXEL_SCALE));
        
        Texture tex = TextureUtils.loadPixelArtTexture(assetManager, texturePath);
        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        setMaterial(mat);
        setQueueBucket(Bucket.Gui);
        setLocalTranslation(x, y, 5);
    }

    public boolean update(float tpf) {
        lifeTime += tpf;
        
        move(0, floatSpeed * tpf, 0);

        float alpha = 1.0f - (lifeTime / maxLifeTime);
        mat.setColor("Color", new ColorRGBA(1f, 1f, 1f, Math.max(0, alpha)));

        return lifeTime >= maxLifeTime;
    }
}
