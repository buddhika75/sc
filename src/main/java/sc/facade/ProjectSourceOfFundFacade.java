package sc.facade;

import sc.entity.DesignComponentForm;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Dr M H B Ariyaratne, buddhika.ari@gmail.com
 */
@Stateless
public class ProjectSourceOfFundFacade extends AbstractFacade<DesignComponentForm> {

    @PersistenceContext(unitName = "pu")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProjectSourceOfFundFacade() {
        super(DesignComponentForm.class);
    }

}
