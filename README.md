# T2 - Redes
- O objetivo do trabalho visa implementar um programa que faça a transferência de arquivos
através de uma rede simulada. Para a transferência funcionar, será preciso implementar uma
aplicação, que rode em cima de UDP e faça o roteamento dos pacotes entre os hosts.

![img1](https://user-images.githubusercontent.com/13707509/58443311-9c431500-80c7-11e9-9d08-45fd43635233.png)


- Uma máquina poderá ter vários processos de transferência de arquivos, onde cada processo
será considerado uma máquina diferente na rede simulada. Quando um arquivo precisar ser
entregue para uma outra rede, ela precisará ser enviado para o processo que faz o papel de roteador
e este deverá enviar o pacote para a outra máquina.

![img2](https://user-images.githubusercontent.com/13707509/58443352-c4cb0f00-80c7-11e9-9f21-78e1e9d14231.png)


# Rede Ethernet
- O endereço das máquinas da rede simulada deve corresponder a um par <IP,Porta>. Nesse
par <IP, Porta>, IP é o endereço 10.32.... da máquina onde o módulo que implementa um host está
instalado e Porta é o endereço pela qual são enviados/recebidos os dados, ou seja, a porta que o
socket real utiliza para ler e escrever da rede.
- Quando um arquivo precisar ser enviado, deve-se verificar o IP de destino. Caso este seja o
mesmo da máquina que vai enviar o arquivo, deve-se verificar a porta de destino, para que a entrega
seja feita para o processo correto. Caso o endereço de destino seja diferente, o arquivo deve ser
enviado para o host que faz o papel de roteador. Somente um host na rede simulada deve fazer o
papel de roteador.

- Os pacotes que circulam na rede devem possuir um cabeçalho com as informações
necessárias na transferência. Essas informações são: endereço IP origem, endereço IP destino,
porta origem, porta destino e nome do arquivo.
- Todo o processo de recepção e roteamento dos pacotes deve estar impresso na tela. Dessa
forma, será possível visualizar todo o caminho por onde o pacote passou. Além disso, quando o
destino receber um arquivo, o mesmo deve ser sinalizado com uma mensagem na tela, além de ser
armazenado na própria máquina.
- Não existirá um número fixo de hosts, eles poderão ser incluídos a qualquer momento na
rede.
# Regras Gerais
Grupos: grupos de no máximo 3 alunos.
Entrega e apresentações: 10/06
- Apresentação:
  - todos participantes devem estar presentes
- Visualização dos Resultados:
  - demonstração deverá acontecer em mais de uma máquina (no mínimo 2);
  - deve permitir a edição de um pacote a ser enviado a algum destino (IP, porta, dados);
  - na chegada de um pacote ao destino, seu conteúdo deve ser mostrado;
  - deve ser possível visualizar o roteamento realizado.
## IMPORTANTE: 
Não serão aceitos trabalhos entregues fora do prazo. Trabalhos que não compilam
ou que não executam não serão avaliados. Todos os trabalhos serão analisados e comparados.
Caso seja identificada cópia de trabalhos, todos os trabalhos envolvidos receberão nota ZERO
