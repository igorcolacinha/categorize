package com.ifta.categorize.service;

import com.ifta.categorize.model.Fornecedor;
import com.ifta.categorize.model.Nota;
import com.ifta.categorize.model.Produto;
import com.ifta.categorize.repository.FornecedorRepository;
import com.ifta.categorize.repository.NotaRepository;
import com.ifta.categorize.repository.ProdutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class NotaFiscalService {
    private final NotaRepository notaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;

    public NotaFiscalService(NotaRepository notaRepository, FornecedorRepository fornecedorRepository, ProdutoRepository produtoRepository) {
        this.notaRepository = notaRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.produtoRepository = produtoRepository;
    }

    public ResponseEntity<?> cadastrar(String stringUrl, UriComponentsBuilder uriBuilder) {
        return notaRepository.findByUrlIgnoreCase(stringUrl)
                .map(nota -> ResponseEntity.ok().build())
                .orElseGet(() -> {
                    try {
                        Nota nota = processaNota(new Nota(stringUrl));
                        final URI uri = uriBuilder.path("/notas/{id}").buildAndExpand(nota.getId()).toUri();

                        return ResponseEntity.created(uri).body(nota);
                    } catch (IOException e) {
                        log.error("Erro ao cadastrar nota fiscal", e);
                        return ResponseEntity.badRequest().body(e.getMessage());
                    }
                });
    }

    public Nota processaNota(Nota nota) throws IOException {
        final String PARAM_NFC = "p";

        final String[] urlSefaz = nota.getUrl().split("\\?");
        if (urlSefaz.length < 2) {
            throw new IllegalArgumentException("A URL não contém parâmetros");
        }

        final String[] parametros = urlSefaz[1].split("=");
        if (parametros.length < 2) {
            throw new IllegalArgumentException("A URL não contém o parâmetro esperado");
        }

        Document doc = Jsoup.connect(urlSefaz[0])
                .data(PARAM_NFC, parametros[1])
                .get();

        nota.setChave(StringUtils.trimAllWhitespace(Objects.requireNonNull(doc.getElementsByClass("chave").first()).text()));
        nota.setNumero(StringUtils.trimAllWhitespace(processaDadosNota(doc, "Número")));
        nota.setSerie(StringUtils.trimAllWhitespace(processaDadosNota(doc, "Série")));

        final String dataEmissaoString = processaDadosNota(doc, "Emissão");

        if (dataEmissaoString != null) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.of("America/Sao_Paulo"));
            nota.setEmissao(ZonedDateTime.parse(dataEmissaoString.split("(-)")[0].trim(), formatter)
                    .withZoneSameInstant(ZoneId.of("America/Sao_Paulo")));

        }

        final List<Produto> produtos = processaProdutosHtml(doc);
        nota.setProdutos(produtos);

        nota.setFornecedor(processaFornecedor(doc));

        return notaRepository.insert(nota);
    }

    private List<Produto> processaProdutosHtml(Document doc) {
        List<Produto> produtos = new ArrayList<>();

        Elements newsHeadlines = doc.getElementsByTag("tbody");
        for (Element headline : newsHeadlines) {

            final Elements produtosHtml = headline.getElementsByTag("tr");

            for (Element produtoHtml : produtosHtml) {
                String nomeProduto = Objects.requireNonNull(produtoHtml.selectFirst("span.txtTit2")).text();
                String codProduto = Objects.requireNonNull(produtoHtml.selectFirst("span.RCod")).text()
                        .replaceAll("[^0-9]", "");
                String qtdProduto = Objects.requireNonNull(produtoHtml.selectFirst("span.Rqtd")).text()
                        .replaceAll("[^0-9,]", "");
                String undProduto = Objects.requireNonNull(produtoHtml.selectFirst("span.RUN")).text()
                        .split(":")[1];
                String vlUniProduto = Objects.requireNonNull(produtoHtml.selectFirst("span.RvlUnit")).text()
                        .split(":")[1];
                String totalProduto = Objects.requireNonNull(produtoHtml.selectFirst("span.valor")).text();

                Produto produto = new Produto();
                produto.setNome(nomeProduto);
                produto.setCodigo(codProduto);
                produto.setQuantidade(qtdProduto);
                produto.setUnidade(StringUtils.trimAllWhitespace(undProduto));
                produto.setValorUnitario(StringUtils.trimAllWhitespace(vlUniProduto));
                produto.setTotal(totalProduto);

                produtoRepository.save(produto);

                produtos.add(produto);
            }
        }

        return produtos;
    }

    private String processaDadosNota(Document doc, String campo) {
        final Element element = Objects.requireNonNull(
                Objects.requireNonNull(doc.getElementById("infos"))
                        .firstElementChild());
        final List<Node> camposInfo = Objects.requireNonNull(element.getElementsByTag("li").first()).childNodes();

        for (Node info : camposInfo) {
            final Node child = info.firstChild();

            if (child != null && ((TextNode) child).text().contains(campo)) {
                final TextNode node = (TextNode) info.nextSibling();
                if (node != null) {
                    return node.text();
                }
            }
        }
        return null;
    }

    private Fornecedor processaFornecedor(Document doc) {
        final Element divElementosFornecedor = doc.getElementsByClass("txtCenter").first();
        assert divElementosFornecedor != null;

        Fornecedor fornecedor = new Fornecedor();

        if (Objects.nonNull(divElementosFornecedor.getElementById("u20")))
            fornecedor.setNome(divElementosFornecedor.getElementById("u20").text());


        for (Element elemento : divElementosFornecedor.getElementsByClass("text")) {
            for (Node node : elemento.childNodes()) {
                final String text = ((TextNode) node).text();
                if (text.contains("CNPJ")) {
                    fornecedor.setCnpj(text.replaceAll("[^0-9]", ""));
                } else {
                    fornecedor.setEndereco(text);

                }
            }
        }

        return fornecedorRepository.insert(fornecedor);
    }

    public ResponseEntity<?> reprocessar(String urlQrCode, UriComponentsBuilder uriBuilder) {
        return notaRepository.findByUrlIgnoreCase(urlQrCode)
                .map(nota -> {
                    try {
                        nota = processaNota(nota);
                        final URI uri = uriBuilder.path("/notas/{id}").buildAndExpand(nota.getId()).toUri();

                        return ResponseEntity.created(uri).body(nota);
                    } catch (IOException e) {
                        log.error("Erro ao cadastrar nota fiscal", e);
                        return ResponseEntity.badRequest().body(e.getMessage());
                    }
                })
                .orElseGet(() -> {
                    try {
                        Nota nota = processaNota(new Nota(urlQrCode));
                        final URI uri = uriBuilder.path("/notas/{id}").buildAndExpand(nota.getId()).toUri();

                        return ResponseEntity.created(uri).body(nota);
                    } catch (IOException e) {
                        log.error("Erro ao cadastrar nota fiscal", e);
                        return ResponseEntity.badRequest().body(e.getMessage());
                    }
                });
    }

    public List<Nota> listar() {
        return notaRepository.findAll();
    }

    public Nota consultarPorId(String id) {
        return notaRepository.findById(id).orElseThrow(() -> new RuntimeException("Nota não encontrada"));
    }

    public Nota consultarPorChave(String chave) {
        return notaRepository.findByChaveIgnoreCase(chave).orElseThrow(() -> new RuntimeException("Nota não encontrada"));
    }
}
