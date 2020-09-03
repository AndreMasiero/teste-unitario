package br.ce.wcaquino.servicos;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

public class LocacaoService {

    private LocacaoDAO dao;
    private SPCService spcService;
    private EmailService emailService;

    public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws FilmeSemEstoqueException, LocadoraException {
        if (usuario == null) {
            throw new LocadoraException("Usuario vazio");
        }

        if (filmes == null || filmes.isEmpty()) {
            throw new LocadoraException("Lista de filmes vazia");
        }

        if (spcService.possuiNegativacao(usuario)) {
            throw new LocadoraException("Usuário negativado");
        }

        Double precoLocacao = 0D;
        if (!filmes.isEmpty()) {
            Integer aux = 0;
            for (Filme filme : filmes) {
                if (filme.getNome() == null || filme.getNome().equals(""))
                    throw new LocadoraException("Lista de filmes vazia");

                if (filme.getEstoque() == 0) {
                    throw new FilmeSemEstoqueException();
                }

                Double valorFilme = filme.getPrecoLocacao();
                switch (aux) {
                    case 2:
                        valorFilme = valorFilme * 0.75;
                        break;
                    case 3:
                        valorFilme = valorFilme * 0.5;
                        break;
                    case 4:
                        valorFilme = valorFilme * 0.25;
                        break;
                    case 5:
                        valorFilme = 0D;
                        break;

                }

                precoLocacao += valorFilme;

                aux++;
            }
        }


        Locacao locacao = new Locacao();
        locacao.setFilmes(filmes);
        locacao.setUsuario(usuario);
        locacao.setDataLocacao(new Date());
        locacao.setValor(precoLocacao);

        //Entrega no dia seguinte
        Date dataEntrega = new Date();
        dataEntrega = adicionarDias(dataEntrega, 1);
        if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY))
            dataEntrega = adicionarDias(dataEntrega, 1);

        locacao.setDataRetorno(dataEntrega);

        //Salvando a locacao...
        dao.salvar(locacao);

        return locacao;
    }

    public void notificarAtraso() {
        List<Locacao> locacacoes = dao.abterLocacoesPendentes();
        for (Locacao locacao : locacacoes) {
            if (locacao.getDataRetorno().before(new Date()))
                emailService.notificacaoAtraso(locacao.getUsuario());
        }
    }

}