package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

import com.jme3.material.RenderState.BlendMode;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.scene.Spatial.CullHint;

import mygame.entities.Manzana;
import mygame.entities.Canasta;
import mygame.util.TextoFlotanteEfectos;
import mygame.util.TextureUtils;

import java.util.ArrayList;
import java.util.List;


import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Main extends SimpleApplication implements ActionListener {

    private Canasta basket;

    private final List<Manzana> apples = new ArrayList<>();
    private final List<TextoFlotanteEfectos> effects = new ArrayList<>();

    private boolean moveLeft = false;
    private boolean moveRight = false;
    private final float basketSpeed = 1200;

    private float spawnTimer = 0;
    private final float spawnInterval = 1f;
    private final float probabilidadVerde = 0.3f;

    private int score = 0;
    private BitmapText scoreText;
    private BitmapText pauseText;
    private boolean isGameOver = false;
    private boolean isPaused = false;

    private int rojasAtrapadas = 0;
    private int rojasPerdidas = 0;
    private int verdesAtrapadas = 0;

    private static final int META_ROJAS = 50;
    private static final int MAX_ROJAS_PERDIDAS = 3;
    private static final int MAX_VERDES_ATRAPADAS = 3;

    //Dificultad progresiva
    private static final int MANZANAS_POR_NIVEL = 10;
    private static final int NIVEL_MAXIMO = 4;
    private static final float AUMENTO_VELOCIDAD_POR_NIVEL = 0.15f;

    //Sonidos
    private static final String RUTA_MUSICA   = "Sounds/Musica.ogg";
    private static final String RUTA_ATRAPAR  = "Sounds/Atrapar.ogg";
    private static final String RUTA_DAÑO     = "Sounds/Daño.ogg";
    private static final String RUTA_VICTORIA = "Sounds/Victoria.ogg";
    private static final String RUTA_DERROTA  = "Sounds/Derrota.ogg";

    private AudioNode musicaFondo;
    private AudioNode sonidoAtrapar;
    private AudioNode sonidoDano;
    private AudioNode sonidoVictoria;
    private AudioNode sonidoDerrota;

    private Geometry endScreenGeom;

    public static void main(String[] args) {
    Main app = new Main();
    AppSettings settings = new AppSettings(true);
    settings.setResolution(1920, 1080);
    settings.setTitle("Canasta de Manzanas");

        try {
            BufferedImage manzana = leerImagen("/Textures/Manzana_Roja.png");
            BufferedImage icono32 = crearIcono(manzana, 1);
            settings.setIcons(new BufferedImage[] {
                crearIcono(manzana, 4),  // 128x128
                crearIcono(manzana, 2),  // 64x64
                icono32,                 // 32x32
                reducir(icono32, 16)     // 16x16
            });
        } catch (Exception e) {
           System.out.println("Aviso: no se pudo cargar el icono -> " + e.getMessage());
        }

        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }
        private static BufferedImage leerImagen(String ruta) throws IOException {
            InputStream in = Main.class.getResourceAsStream(ruta);
            if (in == null) throw new IOException("no se encontró " + ruta);
        try (in) {
            return ImageIO.read(in);
        }
    }

    private static BufferedImage crearIcono(BufferedImage sprite, int escala) {
        int lienzo = Math.max(32, Math.max(sprite.getWidth(), sprite.getHeight()));
        int tam = lienzo * escala;

        BufferedImage icono = new BufferedImage(tam, tam, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icono.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int x = (lienzo - sprite.getWidth()) / 2 * escala;
        int y = (lienzo - sprite.getHeight()) / 2 * escala;
        g.drawImage(sprite, x, y, sprite.getWidth() * escala, sprite.getHeight() * escala, null);
        g.dispose();
        return icono;
    }

    private static BufferedImage reducir(BufferedImage img, int tam) {
        BufferedImage out = new BufferedImage(tam, tam, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, tam, tam, null);
        g.dispose();
        return out;
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        initBackground();
        initBasket();
        initHUD();
        initInput();
        initAudio();
    }

    private void initBackground() {
        Quad quad = new Quad(cam.getWidth(), cam.getHeight());
        Geometry bg = new Geometry("Background", quad);
        Texture tex = TextureUtils.loadPixelArtTexture(assetManager, "Textures/Fondo.png");

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", tex);
        bg.setMaterial(mat);
        bg.setLocalTranslation(0, 0, -10);
        guiNode.attachChild(bg);
    }

    private void initBasket() {
        basket = new Canasta(assetManager);
        basket.setLocalTranslation(cam.getWidth() / 2f - Canasta.WIDTH / 2f, 20, 0);
        guiNode.attachChild(basket);
    }

    private void initHUD() {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Fuente.fnt");

        //Píxeles nítidos, sin suavizado
        for (int i = 0; i < font.getPageSize(); i++) {
            Texture t = font.getPage(i).getTextureParam("ColorMap").getTextureValue();
            t.setMagFilter(Texture.MagFilter.Nearest);
            t.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
            font.getPage(i).getAdditionalRenderState().setBlendMode(BlendMode.Additive);
        }

        float base = font.getCharSet().getRenderedSize();

        //Texto del HUD
        scoreText = new BitmapText(font, false);
        scoreText.setSize(base * 2); // siempre un entero
        scoreText.setLocalTranslation(20, cam.getHeight() - 50, 10);
        guiNode.attachChild(scoreText);

        pauseText = new BitmapText(font, false);
        pauseText.setSize(base * 3); // siempre un entero
        pauseText.setText("PAUSA");
        pauseText.setLocalTranslation(
                Math.round((cam.getWidth() - pauseText.getLineWidth()) / 2f),
                Math.round((cam.getHeight() + pauseText.getLineHeight()) / 2f),
                60);
        pauseText.setCullHint(CullHint.Always);
        guiNode.attachChild(pauseText);

        updateScoreText();
    }

    private void initInput() {
        inputManager.addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addListener(this, "MoveLeft", "MoveRight");

        inputManager.addMapping("Restart", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Restart");

        // P para pausar / reanudar
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addListener(this, "Pause");
    }

    private void initAudio() {
        musicaFondo = cargarSonido(RUTA_MUSICA, true);
        if (musicaFondo != null) {
            musicaFondo.setLooping(true);
            musicaFondo.setVolume(0.5f);
            musicaFondo.play();
        }
        sonidoAtrapar  = cargarSonido(RUTA_ATRAPAR, false);
        sonidoDano     = cargarSonido(RUTA_DAÑO, false);
        sonidoVictoria = cargarSonido(RUTA_VICTORIA, false);
        sonidoDerrota  = cargarSonido(RUTA_DERROTA, false);
    }

    private AudioNode cargarSonido(String ruta, boolean esMusica) {
        try {
            AudioNode audio = new AudioNode(assetManager, ruta, esMusica ? DataType.Stream : DataType.Buffer);
            audio.setPositional(false);
            rootNode.attachChild(audio);
            return audio;
        } catch (AssetNotFoundException e) {
            System.out.println("Aviso: no se encontró el sonido " + ruta);
            return null;
        }
    }

    private void reproducir(AudioNode efecto) {
        if (efecto != null) efecto.playInstance();
    }

    private void detenerMusica() {
        if (musicaFondo != null) musicaFondo.stop();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Restart")) {
            if (isPressed && isGameOver) {
                resetGame();
            }
            return;
        }

        if (name.equals("Pause")) {
            if (isPressed && !isGameOver) {
                togglePause();
            }
            return;
        }

        if (isGameOver || isPaused) return;
        if (name.equals("MoveLeft")) moveLeft = isPressed;
        if (name.equals("MoveRight")) moveRight = isPressed;
    }

    private void togglePause() {
        isPaused = !isPaused;
        moveLeft = false;
        moveRight = false;
        pauseText.setCullHint(isPaused ? CullHint.Inherit : CullHint.Always);

        if (musicaFondo != null) {
            if (isPaused) musicaFondo.pause();
            else musicaFondo.play();
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (isGameOver || isPaused) return;

        updateBasketMovement(tpf);
        updateSpawning(tpf);
        updateApples(tpf);
        updateEffects(tpf);
    }

    private void updateBasketMovement(float tpf) {
        float x = basket.getX();
        if (moveLeft) x -= basketSpeed * tpf;
        if (moveRight) x += basketSpeed * tpf;
        x = Math.max(0, Math.min(cam.getWidth() - Canasta.WIDTH, x));
        basket.setX(x);
    }

    private void updateSpawning(float tpf) {
        spawnTimer += tpf;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
            spawnApple();
        }
    }

    private int getNivel() {
        return Math.min(rojasAtrapadas / MANZANAS_POR_NIVEL, NIVEL_MAXIMO);
    }

    private float getMultiplicadorVelocidad() {
        return 1f + getNivel() * AUMENTO_VELOCIDAD_POR_NIVEL;
    }

    private void spawnApple() {
        Manzana.Tipo tipo = (Math.random() < probabilidadVerde) ? Manzana.Tipo.VERDE : Manzana.Tipo.ROJA;
        Manzana apple = new Manzana(assetManager, tipo);
        apple.setMultiplicadorVelocidad(getMultiplicadorVelocidad());
        float x = (float) (Math.random() * (cam.getWidth() - Manzana.SIZE));
        apple.setLocalTranslation(x, cam.getHeight(), 1);
        guiNode.attachChild(apple);
        apples.add(apple);
    }

    private void updateApples(float tpf) {
        List<Manzana> toRemove = new ArrayList<>();
        float settleDepth = 3 * TextureUtils.PIXEL_SCALE;

        for (Manzana apple : apples) {
            apple.update(tpf);

            if (!apple.isCaught()) {
                if (checkCatch(apple.getX(), apple.getY())) {
                    catchApple(apple);
                    continue;
                }
                if (apple.getY() < -Manzana.SIZE) {
                    if (apple.getTipo() == Manzana.Tipo.ROJA) {
                        onRedAppleMissed(apple.getX());
                        if (isGameOver) { toRemove.add(apple); break; }
                    }
                    toRemove.add(apple);
                }
            } else {
                if (apple.getY() <= settleDepth) {
                    float scoreX = apple.getWorldTranslation().x;
                    float scoreY = apple.getWorldTranslation().y;

                    if (apple.getTipo() == Manzana.Tipo.ROJA) {
                        onRedAppleCaught(scoreX, scoreY);
                    } else {
                        onGreenAppleCaught(scoreX, scoreY);
                    }
                    toRemove.add(apple);
                    if (isGameOver) break;
                }
            }
        }

        for (Manzana apple : toRemove) {
            apples.remove(apple);
            if (apple.isCaught()) {
                basket.detachChild(apple);
            } else {
                guiNode.detachChild(apple);
            }
        }
    }

    private boolean checkCatch(float appleX, float appleY) {
        float scale = TextureUtils.PIXEL_SCALE;

        float basketLeft = basket.getX() + (3 * scale);
        float basketRight = basket.getX() + Canasta.WIDTH - (3 * scale);

        float catchThreshold = basket.getY() + (Canasta.HEIGHT * 0.4f);
        float basketBottom = basket.getY();

        float appleLeft = appleX;
        float appleRight = appleX + Manzana.SIZE;
        float appleBottom = appleY;

        boolean horizontalMatch = (appleRight > basketLeft) && (appleLeft < basketRight);
        boolean verticalMatch = (appleBottom <= catchThreshold) && (appleBottom > basketBottom);

        return horizontalMatch && verticalMatch;
    }

    private void updateEffects(float tpf) {
        List<TextoFlotanteEfectos> toRemove = new ArrayList<>();
        for (TextoFlotanteEfectos effect : effects) {
            if (effect.update(tpf)) {
                toRemove.add(effect);
            }
        }
        for (TextoFlotanteEfectos effect : toRemove) {
            effects.remove(effect);
            guiNode.detachChild(effect);
        }
    }

    private void onRedAppleCaught(float x, float y) {
        score += 1;
        rojasAtrapadas++;
        updateScoreText();
        spawnEffect("Textures/+1.png", x, y);

        if (rojasAtrapadas >= META_ROJAS) {
            triggerWin();
        } else {
            reproducir(sonidoAtrapar);
        }
    }

    private void onRedAppleMissed(float x) {
        score -= 1;
        rojasPerdidas++;
        updateScoreText();
        spawnEffect("Textures/-1.png", x, 0);
        reproducir(sonidoDano);

        if (rojasPerdidas >= MAX_ROJAS_PERDIDAS) {
            triggerGameOver("Se cayeron " + MAX_ROJAS_PERDIDAS + " manzanas rojas");
        }
    }

    private void onGreenAppleCaught(float x, float y) {
        score -= 5;
        verdesAtrapadas++;
        updateScoreText();
        spawnEffect("Textures/-5.png", x, y);
        reproducir(sonidoDano);

        if (verdesAtrapadas >= MAX_VERDES_ATRAPADAS) {
            triggerGameOver("Atrapaste " + MAX_VERDES_ATRAPADAS + " manzanas verdes");
        }
    }

    private void spawnEffect(String texturePath, float x, float y) {
        TextoFlotanteEfectos effect = new TextoFlotanteEfectos(assetManager, texturePath, x, y);
        guiNode.attachChild(effect);
        effects.add(effect);
    }

    private void updateScoreText() {
        scoreText.setText(
                "   Nivel: " + (getNivel() + 1) +
                "   Rojas: " + rojasAtrapadas + "/" + META_ROJAS +
                "   Perdidas: " + rojasPerdidas + "/" + MAX_ROJAS_PERDIDAS +
                "   Verdes: " + verdesAtrapadas + "/" + MAX_VERDES_ATRAPADAS
        );
    }

    private void triggerWin() {
        isGameOver = true;
        detenerMusica();
        reproducir(sonidoVictoria);
        showEndScreen("Textures/Victoria.png");
        scoreText.setCullHint(CullHint.Always);
        System.out.println("¡El jugador ganó la partida!");
    }

    private void triggerGameOver(String motivo) {
        isGameOver = true;
        detenerMusica();
        reproducir(sonidoDerrota);
        showEndScreen("Textures/GameOver.png");
        scoreText.setCullHint(CullHint.Always);
        System.out.println("Has perdido la partida: " + motivo);
    }

    private void catchApple(Manzana apple) {
        float worldX = apple.getX();
        float worldY = apple.getY();

        guiNode.detachChild(apple);
        basket.attachChild(apple);
        apple.setLocalTranslation(worldX - basket.getX(), worldY - basket.getY(), 1);
        apple.setCaught(true);
    }

    private void showEndScreen(String texturePath) {
        Texture tex = TextureUtils.loadPixelArtTexture(assetManager, texturePath);
        float w = tex.getImage().getWidth() * TextureUtils.PIXEL_SCALE;
        float h = tex.getImage().getHeight() * TextureUtils.PIXEL_SCALE;

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

        endScreenGeom = new Geometry("EndScreen", new Quad(w, h));
        endScreenGeom.setMaterial(mat);
        endScreenGeom.setQueueBucket(Bucket.Gui);
        endScreenGeom.setLocalTranslation(
                (cam.getWidth() - w) / 2f,
                (cam.getHeight() - h) / 2f,
                50
        );
        guiNode.attachChild(endScreenGeom);
    }

    private void resetGame() {
        for (Manzana apple : apples) {
            if (apple.isCaught()) {
                basket.detachChild(apple);
            } else {
                guiNode.detachChild(apple);
            }
        }
        apples.clear();

        for (TextoFlotanteEfectos effect : effects) {
            guiNode.detachChild(effect);
        }
        effects.clear();

        if (endScreenGeom != null) {
            guiNode.detachChild(endScreenGeom);
            endScreenGeom = null;
        }

        score = 0;
        rojasAtrapadas = 0;
        rojasPerdidas = 0;
        verdesAtrapadas = 0;
        spawnTimer = 0;
        moveLeft = false;
        moveRight = false;
        isPaused = false;
        pauseText.setCullHint(CullHint.Always);

        basket.setLocalTranslation(cam.getWidth() / 2f - Canasta.WIDTH / 2f, 20, 0);

        updateScoreText();
        scoreText.setCullHint(CullHint.Inherit);

        if (musicaFondo != null) {
            musicaFondo.stop();
            musicaFondo.play();
        }

        isGameOver = false;
    }
}