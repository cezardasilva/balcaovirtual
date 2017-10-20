<template>
  <div>
    <b-modal id="processosMultiplos" v-model="showModal" title="Cota nos Autos" close-title="Cancelar" ok-title="Prosseguir" hide-header-close @hide="save">
      <form>
        <label class="control-label" for="texto" style="width: 100%">Texto</label>
        <b-form-input type="text" list="lst_cotas" name="texto" id="texto" v-model="texto" class="form-control" aria-describedby="cotaHelp" :class="{'is-invalid': errors.has('texto') }" style="width: 100%" autofocus v-validate.initial="'required'"></b-form-input>
        <datalist id="lst_cotas">
          <option v-for="t in textos">{{t}}</option>
        </datalist>
        <span v-if="false" v-show="errors.has('texto')" class="help is-danger">{{ errors.first('texto') }}</span>
        <small id="cotaHelp" class="form-text text-muted">
          <strong>Atenção</strong>! Ao clicar em prosseguir, o texto acima será convertido em um PDF e instantaneamente protocolado na forma de uma Petição Intercorrente. Por favor, verifique o texto antes de clicar em 'Prosseguir'.</small>
        <em v-if="errormsg &amp;&amp; errormsg !== ''" for="processos" class="invalid">{{errormsg}}</em>
      </form>
    </b-modal>
  </div>
</template>

<script>
import UtilsBL from '../bl/utils.js'

var textos = [
  'A União (Fazenda Nacional) requer o ingresso no feito e vista dos autos após as Informações da Autoridade Coatora',
  'A União (Fazenda Nacional) requer o arquivamento dos autos, nos termos do art. 40 da LEF e da Portaria PGFN nº 396/2016 (RDCC).',
  'MM. Juízo, ciente, nada a opor.',
  'A União (Fazenda Nacional) informa que a(s) Inscrição(ões) demanda(s) permanecem parceladas',
  'Devolvo os autos tendo em vista que a União (Fazenda Nacional) não é parte'
]

export default {
  name: 'processo-cota',

  props: ['processo', 'orgao'],

  data () {
    return {
      textos: textos,
      showModal: false,
      errormsg: undefined,
      texto: ''
    }
  },

  methods: {
    show: function () {
      this.showModal = true
      this.errormsg = undefined
    },

    cancel: function (e) {
      e.cancel()
    },

    save: function (e) {
      if (e.isOK === undefined) e.cancel()
      if (!e.isOK) return

      if ((this.texto || '') === '') {
        this.errormsg = 'Texto deve ser informado.'
        e.cancel()
        return
      }

      this.$http.post('processo/' + this.processo + '/cotar', {
        orgao: this.orgao,
        nivelsigilo: '0',
        texto: this.texto
      }, { block: true }).then(response => {
        UtilsBL.logEvento('peticionamento', 'enviar', 'cota')
        this.$emit('ok', response.data.status)
      }, error => {
        this.$emit('erro', error.data.errormsg, this.texto)
      })
    }
  }
}
</script>