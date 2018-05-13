package com.proEclipse.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.proEclipse.beans.Utilisateur;
import com.proEclipse.forms.ConnexionForm;

public class Connexion extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String ATT_USER         = "utilisateur";
    public static final String ATT_FORM         = "form";
    public static final String ATT_SESSION_USER = "sessionUtilisateur";
    public static final String ATT_INTERVALLE_CONNEXIONS = "intervalleConnexions";
    public static final String COOKIE_DERNIERE_CONNEXION = "derniereConnexion";
    public static final String FORMAT_DATE = "dd/MM/yyyy HH:mm:ss";
    public static final String VUE              = "/WEB-INF/connexion.jsp";
    public static final String CHAMP_MEMOIRE             = "memoire";
    public static final int    COOKIE_MAX_AGE            = 60 * 60 * 24 * 365;  // 1 an


    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        // Récupération du cookie pour calculer l'intervalle de connexion
    	String derniereConnexion = getCookieValue(request, COOKIE_DERNIERE_CONNEXION);
        if (derniereConnexion != null) {
        	// On récupère la date courante en objet DateTime
        	DateTime dtCourante = new DateTime();
        	// On crée un parser de date avec le format enregistré
        	DateTimeFormatter formatter = DateTimeFormat.forPattern( FORMAT_DATE );
        	// A l'aide du parseur, on récupère la date enregistrée dans le cookie
        	DateTime dtDerniereConnexion = formatter.parseDateTime( derniereConnexion );
        	// On en déduit l'intervalle dans un objet Period
        	Period periode = new Period(dtDerniereConnexion, dtCourante);
        	
        	// On définit le format de l'affichage de l'intervalle à l'aide d'un formateur de période
        	PeriodFormatter periodeFormatter = new PeriodFormatterBuilder()
        			.appendYears().appendSuffix( " an ", " ans " )
        			.appendMonths().appendSuffix( " mois " )
        			.appendDays().appendSuffix( " jour ", " jours " )
        			.appendHours().appendSuffix( " heure ", " heures " )
        			.appendMinutes().appendSuffix( " minute ", " minutes " )
        			.appendSeparator(" et ")
        			.appendSeconds().appendSuffix( " seconde ", " secondes ")
        			.toFormatter();
        	// On formate l'intervalle
        	String intervalleConnexion = periodeFormatter.print(periode);
        	// On ajout l'intervalle à la requête
        	request.setAttribute(ATT_INTERVALLE_CONNEXIONS, intervalleConnexion);
        }

        /* Affichage de la page de connexion */
        this.getServletContext().getRequestDispatcher( VUE ).forward( request, response );
    }

    /* Méthode de récupération de la valeur du cookie si présent */
    private static String getCookieValue( HttpServletRequest request, String nom) {
    	Cookie[] cookies = request.getCookies();
    	if ( cookies != null ) {
    		for (Cookie cookie : cookies ) {
    			if (cookie != null && nom.equals( cookie.getName())) {
    				return cookie.getValue();
    			}
    		}
    	}
    	return null;
    }
    
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        /* Préparation de l'objet formulaire */
        ConnexionForm form = new ConnexionForm();
        /* Traitement de la requête et récupération du bean en résultant */
        Utilisateur utilisateur = form.connecterUtilisateur( request );
        /* Récupération de la session depuis la requête */
        HttpSession session = request.getSession();

        /*
         * Si aucune erreur de validation n'a eu lieu, alors ajout du bean
         * Utilisateur à la session, sinon suppression du bean de la session.
         */
        if ( form.getErreurs().isEmpty() ) {
            session.setAttribute( ATT_SESSION_USER, utilisateur );
        } else {
            session.setAttribute( ATT_SESSION_USER, null );
        }

        /* Si et seulement si la case du formulaire est cochée */
        if ( request.getParameter( CHAMP_MEMOIRE ) != null ) {
            /* Récupération de la date courante */
            DateTime dt = new DateTime();
            /* Formatage de la date et conversion en texte */
            DateTimeFormatter formatter = DateTimeFormat.forPattern( FORMAT_DATE );
            String dateDerniereConnexion = dt.toString( formatter );
            /* Création du cookie, et ajout à la réponse HTTP */
            setCookie( response, COOKIE_DERNIERE_CONNEXION, dateDerniereConnexion, COOKIE_MAX_AGE );
        } else {
            /* Demande de suppression du cookie du navigateur */
            setCookie( response, COOKIE_DERNIERE_CONNEXION, "", 0 );
        }

        /* Stockage du formulaire et du bean dans l'objet request */
        request.setAttribute( ATT_FORM, form );
        request.setAttribute( ATT_USER, utilisateur );

        this.getServletContext().getRequestDispatcher( VUE ).forward( request, response );
    }
 
    private static void setCookie( HttpServletResponse response, String nom, String valeur, int maxAge ) {
        Cookie cookie = new Cookie( nom, valeur );
        cookie.setMaxAge( maxAge );
        response.addCookie( cookie );
    }
}