/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jeu.core;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import jeu.outils.SingletonJDBC;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jeu.Entite.Avatar;
import jeu.Entite.Banane;

/**
 *
 * @author tmebarki
 */
public class Jeu {
    private BufferedImage decor;
    private int score;
    private Banane uneBanane;
    private Avatar avatar;
    private int playerId;
    private Map<Integer, Avatar> autresJoueurs = new HashMap<>();
    private long derniereSync = 0;
    public Jeu(){
        try {
            this.decor = ImageIO.read(getClass().getResource("/Ressources/jungle.png"));
            } catch (IOException ex) {
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.score = 0;
            this.uneBanane = new Banane();
            this.avatar = new Avatar(0); // temporaire
            this.playerId = creerJoueur();
            this.avatar.setId(playerId);
            
    }
    
    private int creerJoueur() {
        try {
            Connection connexion = jeu.outils.SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement(
                "INSERT INTO joueurs (pseudo, x, y, score, last_update) VALUES (?, ?, ?, 0, NOW())",
                java.sql.Statement.RETURN_GENERATED_KEYS
            );
            requete.setString(1, avatar.getPseudo());
            requete.setDouble(2, avatar.getX());
            requete.setDouble(3, avatar.getY());

            requete.executeUpdate();

            ResultSet resultat = requete.getGeneratedKeys();
                if (resultat.next()) { // Est ce que l'insertion a réussi et une clé a été générée ? next() permet de se positionner sur la première ligne du résultat et renvoi true si elle est non vide
                return resultat.getInt(1); // Récupération de la clé générée (id du joueur) 
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    private void envoyerPosition() {
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement(
                "UPDATE joueurs SET x=?, y=?, score=?, last_update=NOW() WHERE id=?"
            );

            requete.setDouble(1, avatar.getX());
            requete.setDouble(2, avatar.getY());
            requete.setInt(3, score);
            requete.setInt(4, playerId);

            requete.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recupererJoueurs() {
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement(
                "SELECT id, x, y FROM joueurs WHERE last_update > NOW() - INTERVAL 2 SECOND"
            );

            ResultSet resultat = requete.executeQuery();

            while (resultat.next()) {
            int id = resultat.getInt("id");

            if (id == playerId) continue;
                    
                double x = resultat.getDouble("x");
                double y = resultat.getDouble("y");
                Avatar a = autresJoueurs.get(id);
                if (a == null) {
                    System.out.println("Avatar créé avec id = " + id);
                    a = new Avatar(id);
                    autresJoueurs.put(id, a);
                } else {
                    a.setId(id); 
                }
                a.setX(x);
                a.setY(y);
            }    

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recupererBanane() {
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement(
                "SELECT x, y FROM bananes WHERE id=1"
            );

            ResultSet resultat = requete.executeQuery();

            if (resultat.next()) {
                uneBanane.setX(resultat.getDouble("x"));
                uneBanane.setY(resultat.getDouble("y"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void envoyerBanane() {
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement(
                "UPDATE bananes SET x=?, y=? WHERE id=1"
            );

            requete.setDouble(1, uneBanane.getX());
            requete.setDouble(2, uneBanane.getY());

            requete.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean estControleurBanane() {
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement(
                "SELECT MIN(id) as id FROM joueurs WHERE last_update > NOW() - INTERVAL 2 SECOND"
            );

            ResultSet resultat = requete.executeQuery();

            if (resultat.next()) {
                int idMin = resultat.getInt("id");
                return playerId == idMin;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void supprimerJoueur() {
    try {
        Connection connexion = SingletonJDBC.getInstance().getConnection();

        PreparedStatement requete = connexion.prepareStatement(
            "DELETE FROM joueurs WHERE id=?"
        );

        requete.setInt(1, playerId);
        requete.executeUpdate();

        System.out.println("Joueur supprimé");

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private boolean collision(Avatar joueur, Banane banane) {

        if ((banane.getX() >= joueur.getX() + joueur.getLargeur()) ||
            (banane.getX() + banane.getLargeur() <= joueur.getX()) ||
            (banane.getY() >= joueur.getY() + joueur.getHauteur()) ||
            (banane.getY() + banane.getHauteur() <= joueur.getY())) {
            return false;
        } else {
            return true;
        }
    }

    private Avatar joueurQuiCollisionne() {
        if (collision(avatar, uneBanane)) {
            return avatar;
        }
        for (Avatar a : autresJoueurs.values()) {
            if (collision(a, uneBanane)) {
                return a;
            }
        }
        return null;
    }

    private void ajouterScore(int idJoueur) {
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement(
                "UPDATE joueurs SET score = score + 1 WHERE id=?"
            );

            requete.setInt(1, idJoueur);
            int lignes = requete.executeUpdate();

            System.out.println("UPDATE lignes modifiées = " + lignes);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rendu(Graphics2D contexte){
        contexte.drawImage(this.decor, 0, 0, null);
        contexte.drawString("Score : " + score, 10, 20);
        uneBanane.rendu(contexte);
        avatar.rendu(contexte);
        for (Avatar a : autresJoueurs.values()) {
            a.rendu(contexte);
        }
    }
    public void miseAJour(){
        // 1. sync banane
        if (estControleurBanane()) {
            uneBanane.miseAJour();
            envoyerBanane();
        } else {
            recupererBanane();
        }
        // 2. collision
        Avatar gagnant = joueurQuiCollisionne();
        if (gagnant != null && estControleurBanane()) {
            ajouterScore(gagnant.getId());
            System.out.println("Score ajouté pour id = " + gagnant.getId());
            if (gagnant.getId() == playerId) {
                score++;
            }
            uneBanane.lancer();
            envoyerBanane();
        }
        // 3. Sync Joueur
        avatar.miseAJour();
        if (System.currentTimeMillis() - derniereSync > 40) {
            envoyerPosition();
            recupererJoueurs();
            derniereSync = System.currentTimeMillis();
        }
    }    
    public boolean estTermine(){
        return this.uneBanane.getY()>400;
    }
    
    public boolean collisionEntreAvatarEtBanane() {
        if ((uneBanane.getX() >= avatar.getX() + avatar.getLargeur()) // trop à droite
        || (uneBanane.getX() + uneBanane.getLargeur() <= avatar.getX()) // trop à gauche
        || (uneBanane.getY() >= avatar.getY() + avatar.getHauteur()) // trop en bas
        || (uneBanane.getY() + uneBanane.getHauteur() <= avatar.getY())) { // trop en haut
            return false;
        } else {
            return true;
        }
}
    
    public Avatar getAvatar(){
        return this.avatar;
    }
}
