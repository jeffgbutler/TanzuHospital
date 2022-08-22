<template>
  <div id="app">
    <Header />
    <b-container>
      <NameSearch />
      <PatientDetails :patient-id="-1"/>
    </b-container>
  </div>
</template>

<script>
import Header from './components/Header';
import NameSearch from "@/components/NameSearch";
import PatientDetails from "./components/PatientDetails";

export default {
  name: 'App',
  components: {
    Header,
    NameSearch,
    PatientDetails
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
