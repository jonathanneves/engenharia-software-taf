package com.github.adminfaces.starter.model;

public class Grafico {

	private Usuario aluno;
	private Taf taf;
	private int total;
	private double porcentagem = 0;
		
	public Usuario getAluno() {
		return aluno;
	}
	public void setAluno(Usuario aluno) {
		this.aluno = aluno;
	}
	public Taf getTaf() {
		return taf;
	}
	public void setTaf(Taf taf) {
		this.taf = taf;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}

	public double getPorcentagem() {
		return porcentagem;
	}

	public void setPorcentagem(double porcentagem) {
		this.porcentagem = porcentagem;
	}
	
	
	
}
