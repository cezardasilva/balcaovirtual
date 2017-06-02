package br.jus.trf2.balcaovirtual;

import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crivano.swaggerservlet.PresentableException;
import com.crivano.swaggerservlet.SwaggerAsyncResponse;
import com.crivano.swaggerservlet.SwaggerCall;

import br.jus.trf2.balcaovirtual.IBalcaoVirtual.IProcessoNumeroPecaIdPdfGet;
import br.jus.trf2.balcaovirtual.IBalcaoVirtual.ProcessoNumeroPecaIdPdfGetRequest;
import br.jus.trf2.balcaovirtual.IBalcaoVirtual.ProcessoNumeroPecaIdPdfGetResponse;
import br.jus.trf2.sistemaprocessual.ISistemaProcessual.ProcessoValidarNumeroGetRequest;
import br.jus.trf2.sistemaprocessual.ISistemaProcessual.ProcessoValidarNumeroGetResponse;

public class ProcessoNumeroPecaIdPdfGet implements IProcessoNumeroPecaIdPdfGet {
	private static final Logger log = LoggerFactory.getLogger(ProcessoNumeroPecaIdPdfGet.class);

	@Override
	public void run(ProcessoNumeroPecaIdPdfGetRequest req, ProcessoNumeroPecaIdPdfGetResponse resp) throws Exception {
		Map<String, Object> map = SessionsCreatePost.assertUsuarioAutorizado();
		if (!"giovanni.souza".equals(map.get("username")))
			throw new PresentableException("Operação temporariamente desabilitada.");
		resp.jwt = DownloadJwtFilenameGet.jwt((String) map.get("username"), req.orgao, req.numero, req.id);
	}

	@Override
	public String getContext() {
		return "validar número de processo";
	}

}
