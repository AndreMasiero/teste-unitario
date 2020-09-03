package br.ce.wcaquino.servicos;


import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import matchers.MatchersProprios;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static br.ce.wcaquino.utils.DataUtils.*;
import static builders.FilmeBuilder.umFilme;
import static builders.LocacaoBuilder.umLocacao;
import static builders.UsuarioBuilder.umUsuario;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class LocacaoServiceTest {

    @InjectMocks
    private LocacaoService service;

    @Mock
    private EmailService email;
    @Mock
    private SPCService spcService;
    @Mock
    private LocacaoDAO dao;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void deveAlugarFilme() throws Exception {
        Assume.assumeFalse(verificarDiaSemana(new Date(), Calendar.SATURDAY));

        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = new ArrayList<Filme>();
        filmes.addAll(Arrays.asList(umFilme().agora()));

        //acao
        Locacao locacao = service.alugarFilme(usuario, filmes);

        //verificacao
        error.checkThat(locacao.getValor(), is(equalTo(4.0)));
        error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void deveLancarExcecaoAoAlugarFilmeSemEstoque() throws Exception {
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = new ArrayList<Filme>();
        filmes.addAll(Arrays.asList(umFilme().semEstoque().agora()));

        //acao
        service.alugarFilme(usuario, filmes);
    }

    @Test
    public void naoDeveAlugarFilmeSemFilme() {
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = new ArrayList<Filme>();
        filmes.addAll(Arrays.asList(umFilme().semNome().agora()));

        //acao
        try {
            service.alugarFilme(usuario, filmes);
            fail("Deveria ter lançado excessão");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Lista de filmes vazia"));
        }
    }

    @Test
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
        //cenario
        List<Filme> filmes = new ArrayList<Filme>();
        filmes.addAll(Arrays.asList(umFilme().agora()));

        //acao
        try {
            service.alugarFilme(null, filmes);
            Assert.fail();
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), is("Usuario vazio"));
        }
    }


    @Test
    public void naoDeveAlugarFilmeComFilmeNulo() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = umUsuario().agora();

        exception.expect(LocadoraException.class);
        exception.expectMessage("Lista de filmes vazia");

        //acao
        service.alugarFilme(usuario, null);
    }

    @Test
    public void devePagar75PctNoFilme3() throws FilmeSemEstoqueException, LocadoraException {
        // cenário
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = new ArrayList<Filme>();
        filmes.addAll(Arrays.asList(umFilme().agora(),
                umFilme().agora(),
                umFilme().agora()));

        Locacao resultado = service.alugarFilme(usuario, filmes);

        assertThat(resultado.getValor(), is(11.0));
    }

    @Test
    public void devePagar50PctNoFilme4() throws FilmeSemEstoqueException, LocadoraException {
        // cenário
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = new ArrayList<Filme>();
        filmes.addAll(Arrays.asList(umFilme().agora(),
                umFilme().agora(),
                umFilme().agora(),
                umFilme().agora()));

        Locacao resultado = service.alugarFilme(usuario, filmes);

        assertThat(resultado.getValor(), is(13.0));
    }

    @Test
    public void devePagar25PctNoFilme5() throws FilmeSemEstoqueException, LocadoraException {
        // cenário
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = new ArrayList<Filme>();
        filmes.addAll(Arrays.asList(umFilme().agora(),
                umFilme().agora(),
                umFilme().agora(),
                umFilme().agora(),
                umFilme().agora()));

        Locacao resultado = service.alugarFilme(usuario, filmes);

        assertThat(resultado.getValor(), is(14.0));
    }

    @Test
    public void devePagar100PctNoFilme6() throws FilmeSemEstoqueException, LocadoraException {
        // cenário
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = new ArrayList<Filme>();
        filmes.addAll(Arrays.asList(umFilme().agora(),
                umFilme().agora(),
                umFilme().agora(),
                umFilme().agora(),
                umFilme().agora(),
                umFilme().agora()));

        Locacao resultado = service.alugarFilme(usuario, filmes);

        assertThat(resultado.getValor(), is(14.0));
    }

    @Test
    public void deveDevolverNaSegundaAoALugarNoSabado() throws FilmeSemEstoqueException, LocadoraException {
        Assume.assumeTrue(verificarDiaSemana(new Date(), Calendar.SATURDAY));

        // cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = new ArrayList<Filme>();
        filmes.addAll(Arrays.asList(umFilme().agora()));

        // acao
        Locacao retorno = service.alugarFilme(usuario, filmes);

        // verificacao
        Assert.assertThat(retorno.getDataRetorno(), MatchersProprios.caiNumaSegunda());
    }

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSpc() throws FilmeSemEstoqueException {
        // cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().agora());

        when(spcService.possuiNegativacao(any(Usuario.class))).thenReturn(true);

        // acao
        try {
            service.alugarFilme(usuario, filmes);
            //verificacao
            Assert.fail();
        } catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), is("Usuário negativado"));
        }

        verify(spcService).possuiNegativacao(usuario);
    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas() {
        // cenario
        Usuario usuario = umUsuario().agora();
        Usuario usuario2 = umUsuario().comNome("Usuário em Dia").agora();
        Usuario usuario3 = umUsuario().comNome("Outro Atrasado").agora();

        List<Locacao> locacoes = Arrays.asList(
                umLocacao().comUsuario(usuario2).agora(),
                umLocacao().atrasado().comUsuario(usuario).agora(),
                umLocacao().atrasado().comUsuario(usuario3).agora(),
                umLocacao().atrasado().comUsuario(usuario3).agora()
        );
        when(dao.abterLocacoesPendentes()).thenReturn(locacoes);

        //acao
        service.notificarAtraso();

        //verificacao
        verify(email, times(3)).notificacaoAtraso(any(Usuario.class));
        verify(email).notificacaoAtraso(usuario);
        verify(email, times(2)).notificacaoAtraso(usuario3);
        verify(email, never()).notificacaoAtraso(usuario2);
        verifyNoMoreInteractions(email);
    }

//    public static void main(String[] args) {
//        new BuilderMaster().gerarCodigoClasse(Locacao.class);
//    }
}
