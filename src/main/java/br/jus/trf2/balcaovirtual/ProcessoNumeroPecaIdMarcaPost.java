package br.jus.trf2.balcaovirtual;

import java.util.List;

import com.crivano.swaggerservlet.PresentableUnloggedException;

import br.jus.trf2.balcaovirtual.IBalcaoVirtual.IProcessoNumeroPecaIdMarcaPost;
import br.jus.trf2.balcaovirtual.IBalcaoVirtual.ProcessoNumeroPecaIdMarcaPostRequest;
import br.jus.trf2.balcaovirtual.IBalcaoVirtual.ProcessoNumeroPecaIdMarcaPostResponse;
import br.jus.trf2.balcaovirtual.SessionsCreatePost.Usuario;
import br.jus.trf2.balcaovirtual.SessionsCreatePost.UsuarioDetalhe;
import br.jus.trf2.balcaovirtual.model.Estilo;
import br.jus.trf2.balcaovirtual.model.Marca;
import br.jus.trf2.balcaovirtual.model.Processo;
import br.jus.trf2.balcaovirtual.model.TipoMarcaItem;

public class ProcessoNumeroPecaIdMarcaPost implements IProcessoNumeroPecaIdMarcaPost {

	@Override
	public void run(ProcessoNumeroPecaIdMarcaPostRequest req, ProcessoNumeroPecaIdMarcaPostResponse resp)
			throws Exception {
		Usuario u = SessionsCreatePost.assertUsuario();
		UsuarioDetalhe ud = u.usuarios.get(req.orgao.toLowerCase());

		if (ud == null)
			throw new PresentableUnloggedException("Usuário '" + u.usuario
					+ "' não pode fazer marcações porque não foi autenticado no órgão '" + req.orgao + "'.");

		try (Dao dao = new Dao()) {
			Processo p = dao.obtemProcesso(req.numero, req.orgao, true);
			Marca m = null;
			if (req.idmarca != null)
				m = dao.find(Marca.class, Long.valueOf(req.idmarca));
			else
				m = new Marca();

			// verifica se o estilo está compatível com o usuário
			// (interno/externo)
			Estilo estilo = dao.find(Estilo.class, Long.valueOf(req.idestilo));
			if (estilo == null || estilo.isEstiLgInterno())
				throw new PresentableUnloggedException("Estilo inválido.");

			// verifica se o texto representa um tipo_marca_item
			List<TipoMarcaItem> l = dao.obtemTipoMarcaItens(Long.valueOf(req.idclasse));
			TipoMarcaItem tmi = null;
			String texto = req.texto;
			for (TipoMarcaItem i : l) {
				if (i.getTimiNm() != null && texto != null && texto.startsWith(i.getTimiNm())) {
					tmi = i;
					texto = texto.substring(tmi.getTimiNm().length());
					if (texto.startsWith(" - "))
						texto = texto.substring(3);
					texto = texto.trim();
					if (texto.length() == 0)
						texto = null;
					break;
				}
			}

			m.setMarcIdPeca(req.id != null ? Long.valueOf(req.id) : null);
			m.setProcesso(p);
			m.setTipoMarcaItem(tmi);
			m.setEstilo(estilo);
			m.setMarcTxConteudo(texto);
			m.setMarcNrPagInicial(req.paginicial != null ? Integer.valueOf(req.paginicial) : null);
			m.setMarcNrPagFinal(req.pagfinal != null ? Integer.valueOf(req.pagfinal) : null);
			m.setMarcLgInterno(u.isInterno());
			m.setMarcCdUsu(u.usuario);
			m.setMarcNmUsu(u.nome);
			m.setMarcIeUsu(ud.id);
			m.setMarcIeUnidade(ud.unidade);
			m.setMarcDfAlteracao(dao.obtemData());
			dao.persist(m);

			// Produce response
			resp.marca = new br.jus.trf2.balcaovirtual.IBalcaoVirtual.Marca();
			resp.marca.idmarca = Long.toString(m.getMarcId());
			resp.marca.idpeca = req.id;
			resp.marca.texto = tmi != null ? tmi.getTimiNm() + (texto != null ? " - " + texto : "") : texto;
			resp.marca.idestilo = req.idestilo;
			resp.marca.paginicial = req.paginicial;
			resp.marca.pagfinal = req.pagfinal;
		} catch (Exception e) {
			Dao.rollbackCurrentTransaction();
			throw e;
		}
	}

	@Override
	public String getContext() {
		return "gravar marca";
	}

}
