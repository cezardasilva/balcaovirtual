package br.jus.trf2.balcaovirtual;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import br.jus.trf2.balcaovirtual.IBalcaoVirtual.Marcador;
import br.jus.trf2.balcaovirtual.model.Nota;
import br.jus.trf2.balcaovirtual.model.Orgao;
import br.jus.trf2.balcaovirtual.model.Processo;
import br.jus.trf2.balcaovirtual.model.TipoMarcaItem;

public class Dao implements Closeable {
	private EntityManager em;
	public static ThreadLocal<Dao> current = new ThreadLocal<>();

	public Dao() {
		this.em = PersistenceManager.INSTANCE.getEntityManager();
		current.set(this);
	}

	public Date obtemData() {
		return (Date) em.createNativeQuery("select sysdate() from dual").getSingleResult();
	}

	public Processo obtemProcesso(String numero, String orgaoSigla, boolean criar) {
		// identifica o órgao
		Orgao o = (Orgao) em.createNamedQuery("Orgao.findSigla").setParameter("sigla", orgaoSigla).getSingleResult();

		Processo p = null;
		// verifica se o processo já está cadastrado
		try {
			p = (Processo) em.createNamedQuery("Processo.findNumeroEOrgao").setParameter("numero", numero)
					.setParameter("orgao", o).getSingleResult();
		} catch (NoResultException e) {
			if (criar) {
				// insere um novo processo na tabela
				p = new Processo();
				p.setOrgao(o);
				p.setProcCd(numero);
				this.persist(p);
				// p = obtemProcesso(numero, orgaoSigla, false);
			}
		}

		return p;
	}

	public List<Nota> obtemNotas(Processo p, Long ieusuario, Long ieunidade) {
		List<Nota> r = (List<Nota>) em.createNamedQuery("Nota.findProcesso").setParameter("processo", p)
				.setParameter("ieusuario", ieusuario).setParameter("ieunidade", ieunidade).getResultList();
		return r;
	}

	public List<TipoMarcaItem> obtemTipoMarcaItens(Long idclasse) {
		List<TipoMarcaItem> r = (List<TipoMarcaItem>) em.createNamedQuery("TipoMarcaItem.findClasse")
				.setParameter("idclasse", idclasse).getResultList();
		return r;
	}

	public void beginTransaction() {
		this.em.getTransaction().begin();
	}

	public void rollbackTransaction() {
		if (this.em.getTransaction().isActive())
			this.em.getTransaction().rollback();
	}

	public static void rollbackCurrentTransaction() {
		current.get().rollbackTransaction();
	}

	public <T> T find(Class<T> clazz, Long id) {
		return em.find(clazz, id);
	}

	public void persist(Object o) {
		if (!em.getTransaction().isActive())
			beginTransaction();
		this.em.persist(o);
		this.em.flush();
	}

	public void remove(Object o) {
		if (!em.getTransaction().isActive())
			beginTransaction();
		this.em.remove(o);
	}

	@Override
	public void close() throws IOException {
		if (em != null) {
			if (em.getTransaction().isActive()) {
				em.flush();
				em.getTransaction().commit();
			}
			em.close();
		}
	}

}
