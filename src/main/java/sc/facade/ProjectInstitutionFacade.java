package sc.facade;

import sc.entity.SiComponent;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Dr M H B Ariyaratne, buddhika.ari@gmail.com
 */
@Stateless
public class ProjectInstitutionFacade extends AbstractFacade<SiComponent> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProjectInstitutionFacade() {
        super(SiComponent.class);
    }

}
