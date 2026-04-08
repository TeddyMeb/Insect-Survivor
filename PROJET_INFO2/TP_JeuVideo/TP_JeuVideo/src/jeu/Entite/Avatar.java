/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu.Entite;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * @author tmebarki
 */
public class Avatar {

    protected BufferedImage sprite;
    protected double x, y;
    private boolean toucheGauche, toucheDroite;
    private String pseudo;
    private int id;
    public Avatar(int id) {
        try {
            this.sprite = ImageIO.read(getClass().getResource("/Ressources/donkeyKong.png"));
        } catch (IOException ex) {
            Logger.getLogger(Avatar.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.x = 170;
        this.y = 320;
        this.pseudo = "Joueur" + (int)(Math.random() * 1000);
        this.id = id;
        this.toucheGauche = false;
        this.toucheDroite = false;
    }

    public void miseAJour() {
        if (this.toucheGauche) {
            x -= 10;
            }
            if (this.toucheDroite) {
            x += 10;
            }
            if (x > 380 - sprite.getWidth()) { // collision avec le bord droit de la scene
            x = 380 - sprite.getWidth();
            }
            if (x < 0) { // collision avec le bord gauche de la scene
            x = 0;
            }
    }

    public void rendu(Graphics2D contexte) {
        contexte.drawImage(this.sprite, (int) x, (int) y, null);
    }
    public void setToucheGauche(boolean etat) {
        this.toucheGauche = etat;
        }
    
    public void setToucheDroite(boolean etat) {
        this.toucheDroite = etat;
        }
    
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    
    public double getLargeur() {
        return sprite.getWidth();
    }

    public double getHauteur() {
        return sprite.getHeight();
    }

    public void setX(double x2) {
        this.x = x2;
    }

    public void setY(double y2) {
        this.y = y2;
    }
    public String getPseudo() {
        return this.pseudo;
    }
    public int getId() {
    return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}

