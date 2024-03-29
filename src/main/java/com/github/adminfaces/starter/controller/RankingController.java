package com.github.adminfaces.starter.controller;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Session;

import com.github.adminfaces.starter.model.Ranking;
import com.github.adminfaces.starter.model.Taf;
import com.github.adminfaces.starter.model.TafAluno;
import com.github.adminfaces.starter.model.TafExercicio;
import com.github.adminfaces.starter.model.Usuario;
import com.github.adminfaces.starter.util.HibernateUtil;

@ManagedBean
@ViewScoped
public class RankingController implements Serializable {

private static final long serialVersionUID = 1L;

	private Session sessao;

	private TafAluno tafaluno;
 
	private Taf tafselecionado;
	private Ranking rankingSel;
	private TafExercicio exercicioSel;
	private String filtroSel;
	
	private List<Ranking> rankinglista;
	private List<TafAluno> tafalunos;
	private List<TafExercicio> tafexercicios;
	private List<Usuario> usuarios;

	private boolean desativadoPont = true;			//desativar Botao antes de selecionar 3 combo
	private boolean desativadoExer = true;

	@PostConstruct
	public void inicializa() {
		sessao = HibernateUtil.getFabricaDeSessoes().openSession();
		List<Taf> tafs = listarTafsRealizadas();
		if(!tafs.isEmpty())
			tafselecionado = tafs.get(listarTafsRealizadas().size() -1);
		else
			tafselecionado = null;
		listarTafExercicios();
		rankinglista = listarTotalPontos();
	}
	
	//Listar apenas os TAFExercicio da taf selecionada
	public List<TafExercicio> listarTafExercicios() {
		String sql = "SELECT te FROM TafExercicio te WHERE te.taf = " + tafselecionado.getId();
		TypedQuery<TafExercicio> query = sessao.createQuery(sql, TafExercicio.class);
		tafexercicios = query.getResultList();
		return tafexercicios;
	}
	
	//Listar Taf realizadas	
	public List<Taf> listarTafsRealizadas() {
		String sql = "SELECT t FROM Taf t WHERE t.realizado = 'S'";
		TypedQuery<Taf> query = sessao.createQuery(sql, Taf.class);
		return query.getResultList();
	}
	
	//Listar todos os alunos que participaram do TAF
	public List<TafAluno> filtrarAlunosTaf(){
		String sql = "SELECT ta FROM TafAluno ta WHERE ta.tafexercicio.taf = " + tafselecionado.getId();
		TypedQuery<TafAluno> query = sessao.createQuery(sql, TafAluno.class);
		tafalunos = query.getResultList();
		return tafalunos;
	}
	 
	//Listar apenas os TAFExercicio da taf selecionada
	public List<TafAluno> listarPontosExercicio() {
		String sql = "SELECT ta FROM TafAluno ta WHERE ta.tafexercicio.taf = " + tafselecionado.getId()+
				" AND ta.usuario.nome != "+ rankingSel.getTexto();
		TypedQuery<TafAluno> query = sessao.createQuery(sql, TafAluno.class);
		return query.getResultList();
	}
	
	//Filtrar todos os alunos que realizaram o exercicio
	public List<TafAluno> filtrarPorExercicio(){
		String sql = "SELECT ta FROM TafAluno ta WHERE ta.tafexercicio.exercicio = " + exercicioSel.getId()+ 
				" AND ta.tafexercicio.taf = " + tafselecionado.getId();
		TypedQuery<TafAluno> query = sessao.createQuery(sql, TafAluno.class);
		return query.getResultList();
	}
	 
	//Filtrar alunos por fraco forte e médio
	public List<TafAluno> filtrarAlunosClassificados(){
		List<TafAluno> alunosclassificados = filtrarPorExercicio();
		try {
			if(filtroSel.equals("Fraco")) {
				if(exercicioSel.getModalidade().equals("1RM"))
					alunosclassificados.removeIf(s -> s.getTafexercicio().getExercicio().getUmrmFraco() < s.getPontuacao());
				if(exercicioSel.getModalidade().equals("MAX"))
					alunosclassificados.removeIf(s -> s.getTafexercicio().getExercicio().getRmFraco() < s.getPontuacao());
				if(exercicioSel.getModalidade().equals("VT"))
					alunosclassificados.removeIf(s -> s.getTafexercicio().getExercicio().getVtFraco() < s.getPontuacao());
			}else if(filtroSel.equals("Forte")) {
				if(exercicioSel.getModalidade().equals("1RM")) 
					alunosclassificados.removeIf(s -> s.getTafexercicio().getExercicio().getUmrmForte() > s.getPontuacao());
				if(exercicioSel.getModalidade().equals("MAX"))
					alunosclassificados.removeIf(s -> s.getTafexercicio().getExercicio().getRmForte() > s.getPontuacao());
				if(exercicioSel.getModalidade().equals("VT"))
					alunosclassificados.removeIf(s -> s.getTafexercicio().getExercicio().getVtForte() > s.getPontuacao());
			}else if(filtroSel.equals("Médio")) {
				if(exercicioSel.getModalidade().equals("1RM")) {
					alunosclassificados.removeIf(s -> s.getTafexercicio().getExercicio().getUmrmFraco() >= s.getPontuacao());
					alunosclassificados.removeIf(s-> s.getTafexercicio().getExercicio().getUmrmForte() <= s.getPontuacao());
				}
				if(exercicioSel.getModalidade().equals("MAX")) {
					alunosclassificados.removeIf(s -> s.getTafexercicio().getExercicio().getRmFraco() >= s.getPontuacao());
					alunosclassificados.removeIf(s -> s.getTafexercicio().getExercicio().getRmForte() <= s.getPontuacao());
				}
				if(exercicioSel.getModalidade().equals("VT")) {
					alunosclassificados.removeIf(s -> s.getTafexercicio().getExercicio().getVtFraco() >= s.getPontuacao());
					alunosclassificados.removeIf(s -> s.getTafexercicio().getExercicio().getVtForte() <= s.getPontuacao());
				}
			}
			alunosclassificados.sort(Comparator.comparing(TafAluno::getPontuacao).reversed());
		} catch (Exception e) {
			addErro("ERRO", "Erro ao classficar alunos");
		} 
		return alunosclassificados;
	}
	
	public List<Ranking> listarTotalPontos() {
		Session sessao = HibernateUtil.getFabricaDeSessoes().openSession();

		String sql = "SELECT t.taf, a.usuario, SUM(a.pontuacao) as total FROM TafAluno a, TafExercicio t " + 
				"WHERE a.tafexercicio = t.id " +
				"AND t.taf.id = "+tafselecionado.getId() +
				"GROUP BY t.taf, a.usuario " +
				"ORDER BY t.taf, total DESC";
		
		Query query = sessao.createQuery(sql);
		@SuppressWarnings("unchecked")
		List<Object[]> resultados = query.getResultList();
		
		List<Ranking> dados = new ArrayList<Ranking>();
		int pos = 1;
		for(Object[]c:resultados) {
			Ranking rank = new Ranking();
			rank.setPosicao(pos+"º");
			Usuario user = new Usuario();
			user = (Usuario) c[1];
			rank.setTexto(user.getNome());
			rank.setTotalpontos(Math.round((Long) c[2]));
			dados.add(rank);
			pos++;
		}		
		
		rankinglista = dados;
		for(Ranking r : dados)
			System.out.println(r.getPosicao()+"-"+r.getTexto()+"-"+r.getTotalpontos());
		
		return rankinglista;
	}
	  
	 /* public List<Ranking> ranqueandoAluno() {

		  Session sessao = HibernateUtil.getFabricaDeSessoes().openSession();
			
			String sql = "SELECT t.taf, a.usuario, SUM(a.pontuacao) as total FROM TafAluno a, TafExercicio t " + 
					"WHERE a.tafexercicio = t.id " +
					"AND t.taf.id = "+tafselecionado.getId() +
					"GROUP BY t.taf, a.usuario " +
					"ORDER BY t.taf, total DESC";
			
			Query query = sessao.createQuery(sql);
			@SuppressWarnings("unchecked")
			List<Object[]> resultados = query.getResultList();
			
			List<Ranking> dados = new ArrayList<Ranking>();
			int pos = 1;
			for(Object[]c:resultados) {
				Ranking rank = new Ranking();
				rank.setPosicao(pos+"º");
				Usuario user = new Usuario();
				user = (Usuario) c[1];
				rank.setTexto(user.getNome());
				rank.setTotalpontos(Math.round((Long) c[2]));
				dados.add(rank);
				pos++;
			}		
			for(Ranking r : dados)
				System.out.println(r.getPosicao()+"-"+r.getTexto()+"-"+r.getTotalpontos());
			
			return dados;
			
		  List<Ranking>corpo = new ArrayList<Ranking>();
		  String texto="";
		  int saida=0;
		  boolean novoAluno = true;
		  int total = 0; 
		  List<TafAluno> alunos = filtrarAlunosTaf();
		  Collections.sort(alunos, (o1, o2) -> o1.getUsuario().getId().compareTo(o2.getUsuario().getId()));
		  
		  for(TafAluno te : alunos) {
			  if(novoAluno) {
				  texto = te.getUsuario().getNome()+" |   ";
			  	  novoAluno = false;
			  }
			  texto += "   "+te.getPontuacao()+"   |   ";
			  total += te.getPontuacao();
			  saida++;
			  if(tafexercicios.size() == saida) {
				  Ranking rank = new Ranking();
				  rank.setTexto(texto);
				  rank.setTotalpontos(total);
				  corpo.add(rank);
				  novoAluno = true;
				  total=0; saida=0;
			  }
		  }
		  corpo.sort(Comparator.comparing(Ranking::getTotalpontos).reversed());
		  int pos = 0;
		  for(Ranking m : corpo) {
			  pos++;
			  m.setPosicao(pos+"º "); 
		  }
		  return corpo;
	  }
	  
	public String criarCabecalho() {
	   
		String cab = "          Aluno  |";	       
	   for(TafExercicio te : tafexercicios) {
	    		cab +=  " "+te.getExercicio().getNome()+"-"+te.getModalidade()+" | ";
	    }
	   cab += " Total";
	   return cab;
	} */
	    
	public void manterTaf() {
		System.out.println("Nome: "+getTafselecionado().getNome() +"  Data: "+ getTafselecionado().getData());
		listarTafExercicios();
		rankinglista = listarTotalPontos();
	}

	public void manterExercicio() {
		System.out.println("Nome: "+getExercicioSel().getExercicio().getNome() +"  Modalidade: "+ getExercicioSel().getModalidade());
		setDesativadoExer(false);
	}
	
	public void manterFiltro() {
		System.out.println("FILTRO: "+filtroSel);
		setDesativadoPont(false);
	}
	
	public void selecionarRanking(ActionEvent evt){
		rankingSel = (Ranking)evt.getComponent().getAttributes().get("rankingSeleciona");
	}
	
	public List<Ranking> getRankinglista() {
		return rankinglista;
	}

	public void setRankinglista(List<Ranking> rankinglista) {
		this.rankinglista = rankinglista;
	}

	public Ranking getRankingSel() {
		return rankingSel;
	}

	public void setRankingSel(Ranking rankingSel) {
		this.rankingSel = rankingSel;
	}

	public Taf getTafselecionado() {
		return tafselecionado;
	}

	public void setTafselecionado(Taf tafselecionado) {
		this.tafselecionado = tafselecionado;
	}

	public void selecionarExercicio(ActionEvent evt){
		exercicioSel = (TafExercicio)evt.getComponent().getAttributes().get("exercicioSeleciona");
		System.out.println("Exercicio: "+ exercicioSel.getExercicio().getNome());
	}
		
	public List<Usuario> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(List<Usuario> usuarios) {
		this.usuarios = usuarios;
	}

	public TafAluno getTafaluno() {
		return tafaluno;
	}

	public void setTafaluno(TafAluno tafaluno) {
		this.tafaluno = tafaluno;
	}

	public List<TafAluno> getTafalunos() {
		return tafalunos;
	}

	public void setTafalunos(List<TafAluno> tafalunos) {
		this.tafalunos = tafalunos;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public TafExercicio getExercicioSel() {
		return exercicioSel;
	}

	public void setExercicioSel(TafExercicio exercicioSel) {
		this.exercicioSel = exercicioSel;
	}

	public void addErro(String summary, String detail) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
	
	public void addMessage(String summary, String detail) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public String getFiltroSel() {
		return filtroSel;
	}

	public void setFiltroSel(String filtroSel) {
		this.filtroSel = filtroSel;
	}

	public boolean isDesativadoPont() {
		return desativadoPont;
	}

	public void setDesativadoPont(boolean desativadoPont) {
		this.desativadoPont = desativadoPont;
	}

	public boolean isDesativadoExer() {
		return desativadoExer;
	}

	public void setDesativadoExer(boolean desativadoExer) {
		this.desativadoExer = desativadoExer;
	}

	public List<TafExercicio> getTafexercicios() {
		return tafexercicios;
	}

	public void setTafexercicios(List<TafExercicio> tafexercicios) {
		this.tafexercicios = tafexercicios;
	}
}