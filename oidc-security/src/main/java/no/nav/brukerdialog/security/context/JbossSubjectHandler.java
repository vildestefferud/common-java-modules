package no.nav.brukerdialog.security.context;

import org.jboss.security.SecurityContextAssociation;

import javax.security.auth.Subject;

public class JbossSubjectHandler extends SubjectHandler {

    @Override
    public Subject getSubject() {
        return SecurityContextAssociation.getSubject();
    }

}
