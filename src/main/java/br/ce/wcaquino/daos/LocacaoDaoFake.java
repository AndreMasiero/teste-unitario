package br.ce.wcaquino.daos;

import br.ce.wcaquino.entidades.Locacao;

import java.util.List;

public class LocacaoDaoFake implements LocacaoDAO {
    @Override
    public void salvar(Locacao locacao) {

    }

    @Override
    public List<Locacao> abterLocacoesPendentes() {
        return null;
    }
}
